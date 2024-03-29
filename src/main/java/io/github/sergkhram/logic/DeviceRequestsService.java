package io.github.sergkhram.logic;

import io.github.sergkhram.data.entity.AppDescription;
import io.github.sergkhram.data.entity.Device;
import io.github.sergkhram.data.entity.DeviceDirectoryElement;
import io.github.sergkhram.data.entity.Host;
import io.github.sergkhram.data.enums.IOSPackageType;
import io.github.sergkhram.data.service.CrmService;
import io.github.sergkhram.data.service.DownloadService;
import io.github.sergkhram.managers.Manager;
import io.github.sergkhram.managers.adb.AdbManager;
import io.github.sergkhram.managers.idb.IdbKtManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static io.github.sergkhram.utils.Utils.getManagerByType;

@Service
@Slf4j
public class DeviceRequestsService {
    @Autowired
    CrmService crmService;
    @Autowired
    HostRequestsService hostRequestsService;
    List<Manager> managers;
    @Autowired
    DownloadService downloadService;

    public DeviceRequestsService(
        AdbManager adbManager,
        IdbKtManager idbManager
    ) {
        managers = List.of(adbManager, idbManager);
    }

    public Device getDeviceInfo(String id)
        throws NoSuchElementException, IllegalArgumentException
    {
        return crmService.getDeviceById(id);
    }

    public List<Device> getDBDevicesList(String stringFilter, String hostId)
        throws NoSuchElementException, IllegalArgumentException
    {
        hostId = (hostId == null || hostId.isEmpty()) ? null : hostId;
        if(stringFilter == null) stringFilter = "";
        if(hostId != null) {
            return crmService.findAllDevices(stringFilter, hostId);
        } else {
            return crmService.findAllDevices(stringFilter);
        }
    }

    public List<Device> getCurrentDevicesList(String hostId) {
        Host host = (hostId != null && !hostId.isEmpty())
            ? hostRequestsService.getHostInfo(hostId)
            : null;
        CopyOnWriteArrayList<Device> currentListOfDevices = new CopyOnWriteArrayList<>();
        managers
            .parallelStream()
            .forEach(
                it -> currentListOfDevices.addAll(it.getListOfDevices(host))
            );
        return currentListOfDevices;
    }

    public Device saveDevice(Device device) {
        crmService.saveDevice(device);
        return crmService.findAllDevices(device.getSerial()).get(0);
    }

    public void deleteDevice(Device device) {
        crmService.deleteDevice(device);
    }

    public void saveDevices(List<Device> devices) {
        crmService.saveDevices(devices);
    }

    public void reboot(Device device) {
        switch (device.getOsType()) {
            case ANDROID: getManagerByType(managers, AdbManager.class).rebootDevice(device);
            case IOS: getManagerByType(managers, IdbKtManager.class).rebootDevice(device);
        }
    }

    public Map<String, String> getDevicesStates() {
        ConcurrentHashMap<String, String> devicesStates = new ConcurrentHashMap<>();
        managers
            .parallelStream()
            .forEach(
                it -> devicesStates.putAll(it.getDevicesStates())
            );
        return devicesStates;
    }

    public String executeShell(Device device, String shellRequest) {
        return getManagerByType(managers, AdbManager.class).executeShell(
            device,
            shellRequest
        );
    }

    public List<DeviceDirectoryElement> getListFiles(Device device, String path, IOSPackageType iosPackageType) {
        switch (device.getOsType()) {
            case ANDROID: return getManagerByType(managers, AdbManager.class).getListFiles(device, path);
            case IOS: return getManagerByType(managers, IdbKtManager.class).getListFiles(device, path, iosPackageType);
            default: return Collections.emptyList();
        }
    }

    public DownloadService.DownloadResponseData download(DownloadService.DownloadRequestData downloadRequestData)
        throws IOException
    {
        return (
                downloadRequestData.getDeviceDirectoryElement().isDirectory != null
                    && downloadRequestData.getDeviceDirectoryElement().isDirectory
                )
                ? downloadService.downloadFolder(downloadRequestData)
                : downloadService.downloadFile(downloadRequestData);
    }

    public File makeScreenshot(Device device, String path) {
        switch (device.getOsType()) {
            case ANDROID: return getManagerByType(managers, AdbManager.class).makeScreenshot(device, path);
            case IOS: return getManagerByType(managers, IdbKtManager.class).makeScreenshot(device, path);
            default: return null;
        }
    }

    public List<AppDescription> getAppsList(Device device) {
        switch (device.getOsType()) {
            case ANDROID: return getManagerByType(managers, AdbManager.class).getAppsList(device);
            case IOS: return getManagerByType(managers, IdbKtManager.class).getAppsList(device);
            default: return List.of();
        }
    }

    public void updateHostStateWithDeviceRemoval(Host host) {
        try {
            Host updatedHost = hostRequestsService.updateHostState(host);
            if (!updatedHost.getIsActive()) deleteAllHostDevices(host);
        } catch (IOException e) {
            log.info(e.getLocalizedMessage());
            deleteAllHostDevices(host);
        }
    }

    private void deleteAllHostDevices(Host host) {
        getDBDevicesList(
            "",
            host.getId()
        ).forEach(this::deleteDevice);
    }
}
