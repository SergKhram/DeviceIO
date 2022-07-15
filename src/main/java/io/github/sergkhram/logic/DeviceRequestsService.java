package io.github.sergkhram.logic;

import io.github.sergkhram.data.entity.Device;
import io.github.sergkhram.data.entity.DeviceDirectoryElement;
import io.github.sergkhram.data.entity.Host;
import io.github.sergkhram.data.enums.IOSPackageType;
import io.github.sergkhram.data.service.CrmService;
import io.github.sergkhram.data.service.DownloadService;
import io.github.sergkhram.managers.Manager;
import io.github.sergkhram.managers.adb.AdbManager;
import io.github.sergkhram.managers.idb.IdbManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static io.github.sergkhram.utils.Utils.getManagerByType;

@Service
@Slf4j
public class DeviceRequestsService {
    @Autowired
    CrmService service;
    @Autowired
    HostRequestsService hostRequestsService;
    List<Manager> managers;
    @Autowired
    DownloadService downloadService;

    public DeviceRequestsService(
        AdbManager adbManager,
        IdbManager idbManager
    ) {
        managers = List.of(adbManager, idbManager);
    }

    public Device getDeviceInfo(String id)
        throws NoSuchElementException, IllegalArgumentException
    {
        return service.getDeviceById(id);
    }

    public List<Device> getDBDevicesList(String stringFilter, String hostId)
        throws NoSuchElementException, IllegalArgumentException
    {
        return getDBDevicesList(stringFilter, hostId.isEmpty() ? null : UUID.fromString(hostId));
    }

    public List<Device> getDBDevicesList(String stringFilter, UUID hostId)
        throws NoSuchElementException, IllegalArgumentException
    {
        if(stringFilter == null) stringFilter = "";
        if(hostId != null && !hostId.toString().isEmpty()) {
            return service.findAllDevices(stringFilter, hostId);
        } else {
            return service.findAllDevices(stringFilter);
        }
    }

    public List<Device> getCurrentDevicesList(String hostId) {
        Host host = hostId != null
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

    public List<Device> getCurrentDevicesList(UUID hostId) {
        return getCurrentDevicesList(String.valueOf(hostId));
    }

    public Device saveDevice(Device device) {
        service.saveDevice(device);
        return service.findAllDevices(device.getSerial()).get(0);
    }

    public void deleteDevice(Device device) {
        service.deleteDevice(device);
    }

    public void saveDevices(List<Device> devices) {
        service.saveDevices(devices);
    }

    public void reboot(Device device) {
        switch (device.getOsType()) {
            case ANDROID: getManagerByType(managers, AdbManager.class).rebootDevice(device);
            case IOS: getManagerByType(managers, IdbManager.class).rebootDevice(device);
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
            case IOS: return getManagerByType(managers, IdbManager.class).getListFiles(device, path, iosPackageType);
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
}
