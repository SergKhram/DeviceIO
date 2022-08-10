package io.github.sergkhram.grpc;

import com.google.protobuf.Empty;
import io.github.sergkhram.data.entity.Device;
import io.github.sergkhram.data.entity.DeviceDirectoryElement;
import io.github.sergkhram.data.entity.Host;
import io.github.sergkhram.data.enums.DeviceType;
import io.github.sergkhram.data.enums.IOSPackageType;
import io.github.sergkhram.data.enums.OsType;
import io.github.sergkhram.grpc.converters.ProtoConverter;
import io.github.sergkhram.proto.*;
import io.grpc.StatusRuntimeException;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.github.sergkhram.Generator.*;
import static io.github.sergkhram.Generator.generateRandomString;
import static io.github.sergkhram.grpc.converters.ProtoConverter.*;
import static io.github.sergkhram.utils.CustomAssertions.*;
import static io.github.sergkhram.utils.CustomAssertions.assertContainsAllWithAllure;

@Epic("DeviceIO")
@Feature("gRPC")
@Story("Devices")
public class DevicesGrpcTest extends GrpcTestsBase {

    @GrpcClient("myClient")
    private DevicesServiceGrpc.DevicesServiceBlockingStub deviceService;

    @BeforeEach
    public void beforeTest() {
        deviceRepository.deleteAll();
        hostRepository.deleteAll();
    }

    @Test
    @DisplayName("Check get device info by id grpc request")
    public void checkGetDeviceRequest() {
        Host host = generateHosts(1).get(0);
        hostRepository.save(host);
        host = hostRepository.findAll().get(0);
        Device iosDevice = generateDevices(host, 1, DeviceType.DEVICE, OsType.IOS).get(0);
        Device androidSimulator = generateDevices(host, 1).get(0);
        deviceRepository.saveAll(List.of(iosDevice, androidSimulator));
        iosDevice = deviceRepository.search(iosDevice.getName()).get(0);
        androidSimulator = deviceRepository.search(androidSimulator.getName()).get(0);
        DeviceProto responseOsDevice = deviceService.getDeviceRequest(DeviceId.newBuilder().setId(iosDevice.getId().toString()).build());
        DeviceProto responseAndroidSimulator = deviceService.getDeviceRequest(DeviceId.newBuilder().setId(androidSimulator.getId().toString()).build());
        assertAllWithAllure(
            List.of(
                prepareAssertion(
                    convertDeviceToDeviceProto(iosDevice),
                    responseOsDevice,
                    true
                ),
                prepareAssertion(
                    convertDeviceToDeviceProto(androidSimulator),
                    responseAndroidSimulator,
                    true
                )
            )
        );
    }

    @Test
    @DisplayName("Check get devices list grpc request")
    public void checkGetDevicesRequest() {
        Host host = generateHosts(1).get(0);
        hostRepository.save(host);
        host = hostRepository.findAll().get(0);
        List<Device> osDevices = generateDevices(host, 10, DeviceType.DEVICE, OsType.IOS);
        List<Device> androidSimulators = generateDevices(host, 10);
        deviceRepository.saveAll(osDevices);
        deviceRepository.saveAll(androidSimulators);
        List<Device> devices = deviceRepository.findAll();
        GetDevicesListResponse response = deviceService.getDevicesListRequest(
            GetDevicesListRequest.newBuilder()
                .setIsSaved(true)
                .build()
        );;
        assertWithAllure(20, response.getDevicesList().size());
        assertContainsAllWithAllure(
            convertDevicesToProtoDevices(devices),
            response.getDevicesList(),
            true
        );
    }

    @Test
    @DisplayName("Check post device grpc request")
    public void checkCreateDeviceRequest() {
        Host host = generateHosts(1).get(0);
        hostRepository.save(host);
        host = hostRepository.findAll().get(0);
        Device iosDevice = generateDevices(host, 1, DeviceType.DEVICE, OsType.IOS).get(0);
        DeviceProto response = deviceService.postDeviceRequest(
            convertToPostDeviceProto(iosDevice)
        );
        String id = response.getId();
        iosDevice.setId(UUID.fromString(id));
        assertWithAllure(
            convertDeviceToDeviceProto(iosDevice),
            response,
            true
        );
        response = deviceService.getDeviceRequest(DeviceId.newBuilder().setId(id).build());
        assertWithAllure(
            convertDeviceToDeviceProto(iosDevice),
            response,
            true
        );
    }

    @Test
    @DisplayName("Check update device grpc request")
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
        DeviceProto response = deviceService.updateDeviceRequest(
            convertToUpdateDeviceProto(device, id)
        );
        device.setId(UUID.fromString(id));
        assertWithAllure(
            convertDeviceToDeviceProto(device),
            response,
            true
        );
        response = deviceService.getDeviceRequest(DeviceId.newBuilder().setId(id).build());
        assertWithAllure(
            convertDeviceToDeviceProto(device),
            response,
            true
        );
    }

    @Test
    @DisplayName("Check delete device grpc request")
    public void checkDeleteDeviceRequest() {
        Host host = generateHosts(1).get(0);
        hostRepository.save(host);
        host = hostRepository.findAll().get(0);
        Device device = generateDevices(host, 1, DeviceType.DEVICE, OsType.IOS).get(0);
        Device savedDevice = deviceRepository.save(device);
        String id = savedDevice.getId().toString();
        deviceService.deleteDeviceRequest(DeviceId.newBuilder().setId(id).build());
        StatusRuntimeException e = Assertions.assertThrows(
            io.grpc.StatusRuntimeException.class,
            () -> deviceService.getDeviceRequest(DeviceId.newBuilder().setId(id).build())
        );
        assertWithAllure(
            "UNKNOWN: No value present",
            e.getLocalizedMessage()
        );
    }

    @Test
    @DisplayName("Check post devices grpc request")
    public void checkCreateDevicesRequest() {
        Host host = generateHosts(1).get(0);
        hostRepository.save(host);
        host = hostRepository.findAll().get(0);
        List<Device> iosDevices = generateDevices(host, 10, DeviceType.DEVICE, OsType.IOS);
        deviceService.postDevicesRequest(
            convertToPostDevicesProto(iosDevices)
        );
        iosDevices = deviceRepository.findAll();
        GetDevicesListResponse response = deviceService.getDevicesListRequest(GetDevicesListRequest.newBuilder().setIsSaved(true).build());
        assertWithAllure(
            iosDevices.stream().map(ProtoConverter::convertDeviceToDeviceProto).collect(Collectors.toList()),
            response.getDevicesList(),
            true
        );
    }

    @Test
    @DisplayName("Check reboot device grpc request")
    public void checkRebootDeviceRequest() {
        Host host = generateHosts(1).get(0);
        hostRepository.save(host);
        host = hostRepository.findAll().get(0);
        Device device = generateDevices(host, 1, DeviceType.DEVICE, OsType.IOS).get(0);
        Device savedDevice = deviceRepository.save(device);
        String id = savedDevice.getId().toString();
        Mockito.doNothing().when(this.idbManager).rebootDevice(savedDevice);
        Assertions.assertDoesNotThrow(
            () -> deviceService.postDeviceRebootRequest(DeviceId.newBuilder().setId(id).build())
        );
    }

    @Test
    @DisplayName("Check get devices states grpc request")
    public void checkGetDevicesStatesRequest() {
        Host host = generateHosts(1).get(0);
        hostRepository.save(host);
        host = hostRepository.findAll().get(0);
        Device device = generateDevices(host, 1, DeviceType.DEVICE, OsType.IOS).get(0);
        Device savedDevice = deviceRepository.save(device);
        Map<String, String> expectedMap = Map.of(savedDevice.getSerial(), "Connected");
        Mockito.doReturn(expectedMap).when(this.idbManager).getDevicesStates();
        DevicesStatesResponse response = deviceService.getDevicesStatesRequest(Empty.getDefaultInstance());
        assertWithAllure(
            expectedMap,
            response.getStatesMap(),
            true
        );
    }

    @Test
    @DisplayName("Check post device execute shell grpc request")
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
        StatusRuntimeException e = Assertions.assertThrows(
            io.grpc.StatusRuntimeException.class,
            () -> deviceService.postExecuteShellRequest(
                ExecuteShellRequest.newBuilder().setId(savedIosDevice.getId().toString()).setBody(shellRequestBody).build()
            )
        );
        Mockito.doReturn(expectedShellResponse).when(this.adbManager).executeShell(savedAndroidDevice, shellRequestBody);
        ExecuteShellResponse androidResponse = deviceService.postExecuteShellRequest(
            ExecuteShellRequest.newBuilder()
                .setId(savedAndroidDevice.getId().toString())
                .setBody(shellRequestBody)
                .build()
        );
        assertAllWithAllure(
            List.of(
                prepareAssertion(
                    "FAILED_PRECONDITION: Execute shell request allowed for ANDROID only",
                    e.getLocalizedMessage(),
                    true
                ),
                prepareAssertion(
                    expectedShellResponse,
                    androidResponse.getResult(),
                    false
                )
            )
        );
    }

    @Test
    @DisplayName("Check get files list grpc request")
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

        GetDeviceFilesResponse iosResponse = deviceService.getFilesListOfDeviceRequest(
            GetDeviceFilesRequest.newBuilder().setId(savedIosDevice.getId().toString()).build()
        );
        GetDeviceFilesResponse androidResponse = deviceService.getFilesListOfDeviceRequest(
            GetDeviceFilesRequest.newBuilder().setId(savedAndroidDevice.getId().toString()).build()
        );
        assertAllWithAllure(
            List.of(
                prepareAssertion(
                    convertDDElementsToDDElementsProto(expectedFiles),
                    iosResponse.getFilesList(),
                    true
                ),
                prepareAssertion(
                    convertDDElementsToDDElementsProto(expectedFiles),
                    androidResponse.getFilesList(),
                    true
                )
            )
        );
    }

    private PostDeviceRequest convertToPostDeviceProto(Device device) {
        Host savedDeviceHost = device.getHost();
        return PostDeviceRequest.newBuilder()
            .setHost(
                HostInfoProto.newBuilder()
                    .setId(savedDeviceHost.getId().toString())
                    .setPort(Objects.requireNonNullElse(savedDeviceHost.getPort(), 0))
                    .setAddress(savedDeviceHost.getAddress())
                    .setName(savedDeviceHost.getName())
                    .build()
            )
            .setDeviceType(device.getDeviceType().equals(DeviceType.DEVICE) ? DeviceTypeProto.DEVICE : DeviceTypeProto.SIMULATOR)
            .setName(device.getName())
            .setIsActive(device.getIsActive())
            .setOsType(device.getOsType().equals(OsType.IOS) ? OsTypeProto.IOS : OsTypeProto.ANDROID)
            .setOsVersion(device.getOsVersion())
            .setSerial(device.getSerial())
            .setState(device.getState())
            .build();
    }

    private UpdateDeviceRequest convertToUpdateDeviceProto(Device device, String id) {
        Host savedDeviceHost = device.getHost();
        return UpdateDeviceRequest.newBuilder()
            .setId(id)
            .setHost(
                HostInfoProto.newBuilder()
                    .setId(savedDeviceHost.getId().toString())
                    .setPort(Objects.requireNonNullElse(savedDeviceHost.getPort(), 0))
                    .setAddress(savedDeviceHost.getAddress())
                    .setName(savedDeviceHost.getName())
                    .build()
            )
            .setDeviceType(device.getDeviceType().equals(DeviceType.DEVICE) ? DeviceTypeProto.DEVICE : DeviceTypeProto.SIMULATOR)
            .setName(device.getName())
            .setIsActive(device.getIsActive())
            .setOsType(device.getOsType().equals(OsType.IOS) ? OsTypeProto.IOS : OsTypeProto.ANDROID)
            .setOsVersion(device.getOsVersion())
            .setSerial(device.getSerial())
            .setState(device.getState())
            .build();
    }

    private PostDevicesRequest convertToPostDevicesProto(List<Device> devices) {
        PostDevicesRequest.Builder builder = PostDevicesRequest.newBuilder();
        devices.stream().forEach(
            it -> builder.addDevices(convertToPostDeviceProto(it))
        );
        return builder.build();
    }
}
