package io.github.sergkhram.api.controllers.device;

import io.github.sergkhram.data.entity.Device;
import io.github.sergkhram.data.service.CrmService;
import kotlin.jvm.Throws;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static io.github.sergkhram.data.converters.Converters.convertModelToJsonNode;

@RestController
@RequestMapping("/api")
@Slf4j
public class DeviceInfoController {
    CrmService service;

    public DeviceInfoController(CrmService service) {
        this.service = service;
    }

    @GetMapping(path = "/device/{id}")
    @Throws(exceptionClasses = Exception.class)
    public ResponseEntity<Object> getDeviceInfoRequest(
        @PathVariable("id") String id
    ) {
        try {
            Device device = getDeviceInfo(id);
            return ResponseEntity.ok().body(convertModelToJsonNode(device));
        } catch (NoSuchElementException|IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("There is no device with id " + id);
        }
    }

    @GetMapping(path = "/devices")
    @Throws(exceptionClasses = Exception.class)
    public ResponseEntity<Object> getDevicesListRequest(
        @RequestParam(value = "stringFilter", required = false) String stringFilter,
        @RequestParam(value = "hostId", required = false) String hostId
    ) {
        try {
            List<Device> devices = getDevicesList(stringFilter, hostId);
            return ResponseEntity.ok().body(convertModelToJsonNode(devices));
        } catch (NoSuchElementException|IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping(path = "/device")
    @Throws(exceptionClasses = Exception.class)
    public ResponseEntity<Object> postDeviceRequest(
        @RequestBody Device device
    ) {
        try {
            Device savedDevice = saveDevice(device);
            return ResponseEntity.ok().body(convertModelToJsonNode(savedDevice));
        } catch (NoSuchElementException|IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping(path = "/device/{id}")
    @Throws(exceptionClasses = Exception.class)
    public ResponseEntity<Object> updateHostRequest(
        @PathVariable(value = "id") String id,
        @RequestBody Device device
    ) {
        try {
            device.setId(UUID.fromString(id));
            Device savedHost = saveDevice(device);
            return ResponseEntity.ok().body(convertModelToJsonNode(savedHost));
        } catch (NoSuchElementException|IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping(path = "/device/{id}")
    @Throws(exceptionClasses = Exception.class)
    public ResponseEntity<Object> deleteDeviceRequest(
        @PathVariable(value = "id") String id
    ) {
        try {
            Device device = getDeviceInfo(id);
            deleteDevice(device);
            return ResponseEntity.accepted().build();
        } catch (NoSuchElementException|IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("There is no device with id " + id);
        }
    }

    public Device getDeviceInfo(String id)
        throws NoSuchElementException, IllegalArgumentException
    {
        return service.getDeviceById(id);
    }

    public List<Device> getDevicesList(String stringFilter, String hostId)
        throws NoSuchElementException, IllegalArgumentException
    {
        if(stringFilter == null) stringFilter = "";
        if(hostId != null && !hostId.isEmpty()) {
            return service.findAllDevices(stringFilter, UUID.fromString(hostId));
        } else {
            return service.findAllDevices(stringFilter);
        }
    }

    public Device saveDevice(Device device) {
        service.saveDevice(device);
        return service.findAllDevices(device.getSerial()).get(0);
    }

    public void deleteDevice(Device device) {
        service.deleteDevice(device);
    }
}