package io.github.sergkhram.api.controllers.device;

import io.github.sergkhram.data.entity.AppDescription;
import io.github.sergkhram.data.service.DownloadService;
import io.github.sergkhram.logic.DeviceRequestsService;
import io.github.sergkhram.data.entity.Device;
import io.github.sergkhram.data.entity.DeviceDirectoryElement;
import io.github.sergkhram.data.enums.IOSPackageType;
import io.github.sergkhram.data.enums.OsType;
import kotlin.jvm.Throws;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static io.github.sergkhram.utils.json.converters.Converters.convertModelToJsonNode;

@RestController
@RequestMapping("/api")
@Slf4j
public class DeviceController {

    @Autowired
    DeviceRequestsService deviceRequestsService;

    @GetMapping(path = "/device/{id}")
    @Throws(exceptionClasses = Exception.class)
    public ResponseEntity<Object> getDeviceRequest(
        @PathVariable("id") String id
    ) {
        try {
            Device device = deviceRequestsService.getDeviceInfo(id);
            return ResponseEntity.ok().body(convertModelToJsonNode(device));
        } catch (NoSuchElementException|IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("There is no device with id " + id);
        }
    }

    @GetMapping(path = "/devices")
    @Throws(exceptionClasses = Exception.class)
    public ResponseEntity<Object> getDevicesListRequest(
        @RequestParam(value = "isSaved") Boolean isSaved,
        @RequestParam(value = "stringFilter", required = false) String stringFilter,
        @RequestParam(value = "hostId", required = false) String hostId
    ) {
        try {
            List<Device> devices = isSaved
                ? deviceRequestsService.getDBDevicesList(stringFilter, hostId)
                : deviceRequestsService.getCurrentDevicesList(hostId);
            return ResponseEntity.ok().body(convertModelToJsonNode(devices));
        } catch (NoSuchElementException|IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getLocalizedMessage());
        }
    }

    @PostMapping(path = "/device")
    @Throws(exceptionClasses = Exception.class)
    public ResponseEntity<Object> postDeviceRequest(
        @RequestBody Device device
    ) {
        try {
            Device savedDevice = deviceRequestsService.saveDevice(device);
            return ResponseEntity.ok().body(convertModelToJsonNode(savedDevice));
        } catch (NoSuchElementException|IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getLocalizedMessage());
        }
    }

    @PutMapping(path = "/device/{id}")
    @Throws(exceptionClasses = Exception.class)
    public ResponseEntity<Object> updateDeviceRequest(
        @PathVariable(value = "id") String id,
        @RequestBody Device device
    ) {
        try {
            device.setId(UUID.fromString(id));
            Device savedDevice = deviceRequestsService.saveDevice(device);
            return ResponseEntity.ok().body(convertModelToJsonNode(savedDevice));
        } catch (NoSuchElementException|IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getLocalizedMessage());
        }
    }

    @DeleteMapping(path = "/device/{id}")
    @Throws(exceptionClasses = Exception.class)
    public ResponseEntity<Object> deleteDeviceRequest(
        @PathVariable(value = "id") String id
    ) {
        try {
            Device device = deviceRequestsService.getDeviceInfo(id);
            deviceRequestsService.deleteDevice(device);
            return ResponseEntity.accepted().build();
        } catch (NoSuchElementException|IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("There is no device with id " + id);
        }
    }

    @PostMapping(path = "/devices")
    @Throws(exceptionClasses = Exception.class)
    public ResponseEntity<Object> postDevicesRequest(
        @RequestBody List<Device> devices
    ) {
        try {
            deviceRequestsService.saveDevices(devices);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException|IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getLocalizedMessage());
        }
    }

    @PostMapping(path = "/device/{id}/reboot")
    @Throws(exceptionClasses = Exception.class)
    public ResponseEntity<Object> postDeviceRebootRequest(
        @PathVariable(value = "id") String id
    ) {
        try {
            Device device = deviceRequestsService.getDeviceInfo(id);
            deviceRequestsService.reboot(device);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException|IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getLocalizedMessage());
        }
    }

    @GetMapping(path = "/devices/states")
    @Throws(exceptionClasses = Exception.class)
    public ResponseEntity<Object> getDevicesStatesRequest() {
        try {
            Map<String, String> states = deviceRequestsService.getDevicesStates();
            return ResponseEntity.ok().body(convertModelToJsonNode(states));
        } catch (NoSuchElementException|IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getLocalizedMessage());
        }
    }

    @PostMapping(path = "/device/{id}/executeShell")
    @Throws(exceptionClasses = Exception.class)
    public ResponseEntity<Object> postExecuteShellRequest(
        @PathVariable(value = "id") String id,
        @RequestBody String body
    ) {
        try {
            Device device = deviceRequestsService.getDeviceInfo(id);
            if(device.getOsType().equals(OsType.IOS))
                return ResponseEntity.badRequest().body("Execute shell request allowed for ANDROID only");

            String result = deviceRequestsService.executeShell(device, body);
            ShellResult shellResult = ShellResult.builder().result(result).build();
            return ResponseEntity.ok().body(convertModelToJsonNode(shellResult));
        } catch (NoSuchElementException|IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getLocalizedMessage());
        }
    }

    @GetMapping(path = "/device/{id}/files")
    @Throws(exceptionClasses = Exception.class)
    public ResponseEntity<Object> getFilesListOfDeviceRequest(
        @PathVariable(value = "id") String id,
        @RequestParam(value = "path", required = false) String path,
        @RequestParam(value = "iosPackageType", required = false) String iosPackageTypeValue
    ) {
        try {
            IOSPackageType iosPackageType = IOSPackageType.APPLICATION;
            Device device = deviceRequestsService.getDeviceInfo(id);
            if(path == null) path = "";
            if(iosPackageTypeValue != null && !iosPackageTypeValue.isEmpty())
                iosPackageType = IOSPackageType.valueOf(iosPackageTypeValue.toUpperCase());
            List<DeviceDirectoryElement> files = deviceRequestsService.getListFiles(device, path, iosPackageType);
            return ResponseEntity.ok().body(convertModelToJsonNode(files));
        } catch (NoSuchElementException|IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getLocalizedMessage());
        }
    }

    @PostMapping(path = "/device/{id}/files/download")
    @Throws(exceptionClasses = Exception.class)
    public ResponseEntity<Object> postDownloadFileRequest(
        @PathVariable(value = "id") String id,
        @RequestParam(value = "iosPackageType", required = false) String iosPackageTypeValue,
        @RequestBody DeviceDirectoryElement deviceDirectoryElement
    ) {
        File currentFile = null;
        try {
            IOSPackageType iosPackageType = IOSPackageType.APPLICATION;
            if(iosPackageTypeValue != null && !iosPackageTypeValue.isEmpty())
                iosPackageType = IOSPackageType.valueOf(iosPackageTypeValue.toUpperCase());
            DownloadService.DownloadRequestData requestData = DownloadService.DownloadRequestData
                .builder()
                .device(deviceRequestsService.getDeviceInfo(id))
                .deviceDirectoryElement(deviceDirectoryElement)
                .iosPackageType(iosPackageType)
                .build();
            DownloadService.DownloadResponseData responseData = deviceRequestsService.download(requestData);

            Path path = Paths.get(responseData.getFile().getAbsolutePath());
            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

            currentFile = responseData.getFile();
            return ResponseEntity.ok()
                .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + responseData.getFile().getName() + "\""
                )
                .contentLength(responseData.getFile().length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getLocalizedMessage());
        } finally {
            if (currentFile != null && currentFile.exists()) {
                FileUtils.deleteQuietly(currentFile);
            }
        }
    }

    @GetMapping(path = "/device/{id}/apps")
    @Throws(exceptionClasses = Exception.class)
    public ResponseEntity<Object> getAppsListRequest(
        @PathVariable(value = "id") String id
    ) {
        try {
            Device device = deviceRequestsService.getDeviceInfo(id);
            List<AppDescription> apps = deviceRequestsService.getAppsList(device);
            return ResponseEntity.ok().body(convertModelToJsonNode(apps));
        } catch (NoSuchElementException|IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getLocalizedMessage());
        }
    }
}