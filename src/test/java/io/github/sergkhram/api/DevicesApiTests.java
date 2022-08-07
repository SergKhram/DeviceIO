package io.github.sergkhram.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.sergkhram.api.controllers.device.ShellResult;
import io.github.sergkhram.api.requests.DevicesRequests;
import io.github.sergkhram.data.entity.Device;
import io.github.sergkhram.data.entity.DeviceDirectoryElement;
import io.github.sergkhram.data.entity.Host;
import io.github.sergkhram.data.enums.DeviceType;
import io.github.sergkhram.data.enums.IOSPackageType;
import io.github.sergkhram.data.enums.OsType;
import io.github.sergkhram.managers.adb.AdbManager;
import io.github.sergkhram.managers.idb.IdbManager;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.github.sergkhram.Generator.*;
import static io.github.sergkhram.utils.CustomAssertions.*;
import static io.github.sergkhram.utils.json.JsonTestUtil.*;

@Epic("DeviceIO")
@Feature("API")
@Story("Devices")
public class DevicesApiTests extends ApiTestsBase{

    @MockBean
    IdbManager idbManager;

    @MockBean
    AdbManager adbManager;

    @BeforeEach
    public void beforeTest() {
        deviceRepository.deleteAll();
        hostRepository.deleteAll();
    }

    @Test
    @DisplayName("Check get device info by id api request")
    public void checkGetDeviceRequest() {
        Host host = generateHosts(1).get(0);
        hostRepository.save(host);
        host = hostRepository.findAll().get(0);
        Device osDevice = generateDevices(host, 1, DeviceType.DEVICE, OsType.IOS).get(0);
        Device androidSimulator = generateDevices(host, 1).get(0);
        deviceRepository.saveAll(List.of(osDevice, androidSimulator));
        osDevice = deviceRepository.search(osDevice.getName()).get(0);
        androidSimulator = deviceRepository.search(androidSimulator.getName()).get(0);
        Response responseOsDevice = DevicesRequests.getDeviceById(getBaseUrl(), osDevice.getId().toString());
        Response responseAndroidSimulator = DevicesRequests.getDeviceById(getBaseUrl(), androidSimulator.getId().toString());
        assertAllWithAllure(
            List.of(
                prepareAssertion(
                    osDevice,
                    responseOsDevice.as(Device.class),
                    false
                ),
                prepareAssertion(
                    androidSimulator,
                    responseAndroidSimulator.as(Device.class),
                    false
                )
            )
        );
    }

    @Test
    @DisplayName("Check get devices list api request")
    public void checkGetDevicesRequest() {
        Host host = generateHosts(1).get(0);
        hostRepository.save(host);
        host = hostRepository.findAll().get(0);
        List<Device> osDevices = generateDevices(host, 10, DeviceType.DEVICE, OsType.IOS);
        List<Device> androidSimulators = generateDevices(host, 10);
        deviceRepository.saveAll(osDevices);
        deviceRepository.saveAll(androidSimulators);
        List<Device> devices = deviceRepository.findAll();
        Response response = DevicesRequests.getDevices(getBaseUrl(), Map.of("isSaved", true));
        List<Device> responseDevices = response.jsonPath().getList("", Device.class);
        assertWithAllure(20, responseDevices.size());
        assertContainsAllWithAllure(devices, responseDevices);
    }

    @Test
    @DisplayName("Check post device api request")
    public void checkCreateDeviceRequest() {
        Host host = generateHosts(1).get(0);
        hostRepository.save(host);
        host = hostRepository.findAll().get(0);
        Device osDevice = generateDevices(host, 1, DeviceType.DEVICE, OsType.IOS).get(0);
        Response response = DevicesRequests.postDevice(getBaseUrl(), convertModelToString(osDevice));
        String id = response.jsonPath().getString("id");
        JsonNode expectedDevice = convertModelToJsonNode(osDevice);
        ((ObjectNode)expectedDevice).put("id", "regexp:(.*)");
        assertWithAllureWRegex(
            expectedDevice,
            response.as(JsonNode.class)
        );
        response = DevicesRequests.getDeviceById(getBaseUrl(), id);
        assertWithAllureWRegex(
            expectedDevice,
            response.as(JsonNode.class)
        );
    }

    @Test
    @DisplayName("Check update device api request")
    public void checkUpdateDeviceRequest() {
        Host host = generateHosts(1).get(0);
        hostRepository.save(host);
        host = hostRepository.findAll().get(0);
        Device device = generateDevices(host, 1, DeviceType.DEVICE, OsType.IOS).get(0);
        Device savedDevice = deviceRepository.save(device);
        String id = savedDevice.getId().toString();
        device.setOsVersion(generateRandomString());
        device.setDeviceType(DeviceType.SIMULATOR);
        device.setSerial(generateRandomString());
        device.setState("Connected");
        device.setIsActive(true);
        device.setOsType(OsType.ANDROID);
        device.setName(generateRandomString());
        Response response = DevicesRequests.updateDevice(getBaseUrl(), id, convertModelToStringWONullValues(device));
        device.setId(UUID.fromString(id));
        JsonNode expectedDevice = convertModelToJsonNode(device);
        assertWithAllureWRegex(
            expectedDevice,
            response.as(JsonNode.class)
        );
        response = DevicesRequests.getDeviceById(getBaseUrl(), id);
        assertWithAllureWRegex(
            expectedDevice,
            response.as(JsonNode.class)
        );
    }

    @Test
    @DisplayName("Check delete device api request")
    public void checkDeleteDeviceRequest() {
        Host host = generateHosts(1).get(0);
        hostRepository.save(host);
        host = hostRepository.findAll().get(0);
        Device device = generateDevices(host, 1, DeviceType.DEVICE, OsType.IOS).get(0);
        Device savedDevice = deviceRepository.save(device);
        String id = savedDevice.getId().toString();
        DevicesRequests.deleteDevice(getBaseUrl(), id);
        Response response = DevicesRequests.getDeviceById(getBaseUrl(), id, 400);
        assertWithAllure(
            "There is no device with id " + id,
            response.asString()
        );
    }

    @Test
    @DisplayName("Check post devices api request")
    public void checkCreateDevicesRequest() {
        Host host = generateHosts(1).get(0);
        hostRepository.save(host);
        host = hostRepository.findAll().get(0);
        List<Device> osDevices = generateDevices(host, 10, DeviceType.DEVICE, OsType.IOS);
        DevicesRequests.postDevices(getBaseUrl(), convertModelToString(osDevices));
        JsonNode expectedDevices = convertModelToJsonNode(osDevices);
        ((ArrayNode)expectedDevices).forEach(it -> ((ObjectNode)it).put("id", "regexp:(.*)"));
        Response response = DevicesRequests.getDevices(getBaseUrl(), Map.of("isSaved", true));
        assertWithAllureWRegex(
            expectedDevices,
            response.as(JsonNode.class)
        );
    }

    @Test
    @DisplayName("Check reboot device api request")
    public void checkRebootDeviceRequest() {
        Host host = generateHosts(1).get(0);
        hostRepository.save(host);
        host = hostRepository.findAll().get(0);
        Device device = generateDevices(host, 1, DeviceType.DEVICE, OsType.IOS).get(0);
        Device savedDevice = deviceRepository.save(device);
        String id = savedDevice.getId().toString();
        Mockito.doNothing().when(this.idbManager).rebootDevice(savedDevice);
        DevicesRequests.postDeviceReboot(getBaseUrl(), id);
    }

    @Test
    @DisplayName("Check get devices states api request")
    public void checkGetDevicesStatesRequest() {
        Host host = generateHosts(1).get(0);
        hostRepository.save(host);
        host = hostRepository.findAll().get(0);
        Device device = generateDevices(host, 1, DeviceType.DEVICE, OsType.IOS).get(0);
        Device savedDevice = deviceRepository.save(device);
        Map<String, String> expectedMap = Map.of(savedDevice.getSerial(), "Connected");
        Mockito.doReturn(expectedMap).when(this.idbManager).getDevicesStates();
        Response response = DevicesRequests.getDevicesStates(getBaseUrl());
        assertWithAllure(
            expectedMap,
            response.as(Map.class)
        );
    }

    @Test
    @DisplayName("Check post device execute shell api request")
    public void checkExecuteShellRequest() {
        Host host = generateHosts(1).get(0);
        hostRepository.save(host);
        host = hostRepository.findAll().get(0);
        Device iosDevice = generateDevices(host, 1, DeviceType.DEVICE, OsType.IOS).get(0);
        Device savedIosDevice = deviceRepository.save(iosDevice);
        Device androidDevice = generateDevices(host, 1, DeviceType.DEVICE, OsType.ANDROID).get(0);
        Device savedAndroidDevice = deviceRepository.save(androidDevice);
        String shellRequestBody = generateRandomString();
        String expectedShellResponse = generateRandomString();
        Response iosResponse = DevicesRequests.executeShell(getBaseUrl(), savedIosDevice.getId().toString(), shellRequestBody, 400);
        Mockito.doReturn(expectedShellResponse).when(this.adbManager).executeShell(savedAndroidDevice, shellRequestBody);
        Response androidResponse = DevicesRequests.executeShell(getBaseUrl(), savedAndroidDevice.getId().toString(), shellRequestBody);
        assertAllWithAllure(
            List.of(
                prepareAssertion(
                    "Execute shell request allowed for ANDROID only",
                    iosResponse.asString(),
                    false
                ),
                prepareAssertion(
                    convertModelToJsonNode(
                        ShellResult
                            .builder()
                            .result(expectedShellResponse)
                            .build()
                    ),
                    androidResponse.as(JsonNode.class),
                    false
                )
            )
        );
    }

    @Test
    @DisplayName("Check get files list api request")
    public void checkGetFilesListRequest() {
        Host host = generateHosts(1).get(0);
        hostRepository.save(host);
        host = hostRepository.findAll().get(0);
        Device iosDevice = generateDevices(host, 1, DeviceType.DEVICE, OsType.IOS).get(0);
        Device savedIosDevice = deviceRepository.save(iosDevice);
        Device androidDevice = generateDevices(host, 1, DeviceType.DEVICE, OsType.ANDROID).get(0);
        Device savedAndroidDevice = deviceRepository.save(androidDevice);
        List<DeviceDirectoryElement> expectedFiles = generateFilesList(10);
        Mockito.doReturn(expectedFiles).when(this.idbManager).getListFiles(savedIosDevice, "", IOSPackageType.APPLICATION);
        Mockito.doReturn(expectedFiles).when(this.adbManager).getListFiles(savedAndroidDevice, "");
        Response iosResponse = DevicesRequests.getFilesList(getBaseUrl(), savedIosDevice.getId().toString());
        Response androidResponse = DevicesRequests.getFilesList(getBaseUrl(), savedAndroidDevice.getId().toString());
        assertAllWithAllure(
            List.of(
                prepareAssertion(
                    convertModelToJsonNode(expectedFiles),
                    iosResponse.as(JsonNode.class),
                    false
                ),
                prepareAssertion(
                    convertModelToJsonNode(expectedFiles),
                    androidResponse.as(JsonNode.class),
                    false
                )
            )
        );
    }
}
