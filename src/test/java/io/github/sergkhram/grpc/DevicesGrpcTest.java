package io.github.sergkhram.grpc;

import io.github.sergkhram.data.entity.Device;
import io.github.sergkhram.data.entity.Host;
import io.github.sergkhram.data.enums.DeviceType;
import io.github.sergkhram.data.enums.OsType;
import io.github.sergkhram.proto.*;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static io.github.sergkhram.Generator.generateDevices;
import static io.github.sergkhram.Generator.generateHosts;
import static io.github.sergkhram.grpc.converters.ProtoConverter.convertDeviceToDeviceProto;
import static io.github.sergkhram.grpc.converters.ProtoConverter.convertDevicesToProtoDevices;
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
}
