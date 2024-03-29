package io.github.sergkhram.grpc.converters;

import io.github.sergkhram.data.entity.AppDescription;
import io.github.sergkhram.data.entity.Device;
import io.github.sergkhram.data.entity.DeviceDirectoryElement;
import io.github.sergkhram.data.entity.Host;
import io.github.sergkhram.data.enums.DeviceType;
import io.github.sergkhram.data.enums.OsType;
import io.github.sergkhram.proto.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProtoConverter {
    public static List<HostProto> convertHostsToHostsProto(List<Host> hosts) {
        return hosts.parallelStream()
            .map(
                ProtoConverter::convertHostToHostProto
            )
            .collect(Collectors.toList());
    }

    public static HostProto convertHostToHostProto(Host host) {
        return HostProto.newBuilder()
            .setId(String.valueOf(host.getId()))
            .setName(host.getName())
            .setAddress(host.getAddress())
            .setIsActive(host.getIsActive())
            .addAllDevices(
                convertDevicesFromHost(host)
            )
            .setPort(
                Objects.requireNonNullElse(
                    host.getPort(),
                    0
                )
            )
            .build();
    }

    public static Host convertHostProtoRequestToHost(PostHostRequest hostRequest) {
        Host host = new Host();
        host.setName(hostRequest.getName());
        host.setAddress(hostRequest.getAddress());
        if(hostRequest.getPort() != 0) host.setPort(hostRequest.getPort());
        return host;
    }

    public static Host convertUpdateHostProtoRequestToHost(UpdateHostRequest hostRequest) {
        Host host = new Host();
        host.setName(hostRequest.getName());
        host.setAddress(hostRequest.getAddress());
        if(hostRequest.getPort() != 0) host.setPort(hostRequest.getPort());
        return host;
    }

    public static List<DeviceProto> convertDevicesFromHost(Host host) {
        try {
            List<Device> emptyList = Collections.emptyList();
            return Objects.requireNonNullElse(host.getDevices(), emptyList).parallelStream().map(
                it -> DeviceProto.newBuilder()
                    .setDeviceType(
                        Objects.requireNonNullElse(
                            it.getDeviceType(), DeviceType.SIMULATOR
                        ).equals(DeviceType.DEVICE)
                            ? DeviceTypeProto.DEVICE
                            : DeviceTypeProto.SIMULATOR
                    )
                    .setId(
                        it.getId() != null
                            ? String.valueOf(it.getId())
                            : ""
                    )
                    .setName(it.getName())
                    .setIsActive(it.getIsActive())
                    .setOsType(
                        it.getOsType().equals(OsType.ANDROID)
                            ? OsTypeProto.ANDROID
                            : OsTypeProto.IOS
                    )
                    .setSerial(it.getSerial())
                    .setState(it.getState())
                    .setOsVersion(it.getOsVersion())
                    .build()
            ).collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public static DeviceProto convertDeviceToDeviceProto(Device device) {
        DeviceProto deviceProto = DeviceProto.newBuilder()
            .setId(
                device.getId() != null
                    ? String.valueOf(device.getId())
                    : ""
                )
            .setName(device.getName())
            .setIsActive(device.getIsActive())
            .setOsType(
                device.getOsType().equals(OsType.ANDROID)
                    ? OsTypeProto.ANDROID
                    : OsTypeProto.IOS
            )
            .setSerial(device.getSerial())
            .setState(device.getState())
            .setOsVersion(device.getOsVersion())
            .setDeviceType(
                Objects.requireNonNullElse(
                    device.getDeviceType(), DeviceType.SIMULATOR
                )
                    .equals(DeviceType.DEVICE)
                        ? DeviceTypeProto.DEVICE
                        : DeviceTypeProto.SIMULATOR
            )
            .build();
        Host host = device.getHost();
        if(host!=null) {
            HostInfoProto hostInfoProto = HostInfoProto.newBuilder()
                .setId(String.valueOf(host.getId()))
                .setName(host.getName())
                .setAddress(host.getAddress())
                .setIsActive(host.getIsActive())
                .build();
            if(host.getPort()!=null) hostInfoProto.toBuilder().setPort(host.getPort()).build();
            deviceProto = deviceProto.toBuilder().setHost(hostInfoProto).build();
        }
        return deviceProto;
    }

    public static List<DeviceProto> convertDevicesToProtoDevices(List<Device> devices) {
        return devices.parallelStream()
            .map(
                ProtoConverter::convertDeviceToDeviceProto
            )
            .collect(Collectors.toList());
    }

    public static Device convertDeviceProtoRequestToDevice(PostDeviceRequest deviceRequest) {
        Device device = new Device();
        device.setDeviceType(
            deviceRequest.getDeviceType().equals(DeviceTypeProto.DEVICE)
                ? DeviceType.DEVICE
                : DeviceType.SIMULATOR
        );
        device.setName(deviceRequest.getName());
        device.setOsType(
            deviceRequest.getOsType().equals(OsTypeProto.ANDROID)
                ? OsType.ANDROID
                : OsType.IOS
        );
        device.setOsVersion(deviceRequest.getOsVersion());
        device.setSerial(deviceRequest.getSerial());
        device.setIsActive(deviceRequest.getIsActive());
        device.setState(deviceRequest.getState());
        device.setHost(convertHostInfoProtoToHost(deviceRequest.getHost()));
        return device;
    }

    public static Device convertUpdateDeviceProtoRequestToDevice(UpdateDeviceRequest deviceRequest) {
        PostDeviceRequest postDeviceRequest = PostDeviceRequest.newBuilder()
            .setDeviceType(deviceRequest.getDeviceType())
            .setHost(deviceRequest.getHost())
            .setName(deviceRequest.getName())
            .setOsType(deviceRequest.getOsType())
            .setOsVersion(deviceRequest.getOsVersion())
            .setSerial(deviceRequest.getSerial())
            .setIsActive(deviceRequest.getIsActive())
            .setState(deviceRequest.getState())
            .build();
        return convertDeviceProtoRequestToDevice(postDeviceRequest);
    }

    public static Host convertHostInfoProtoToHost(HostInfoProto hostInfoProto) {
        Host host = new Host();
        host.setName(hostInfoProto.getName());
        host.setId(hostInfoProto.getId());
        host.setIsActive(hostInfoProto.getIsActive());
        host.setAddress(hostInfoProto.getAddress());
        if(hostInfoProto.getPort()!=0) host.setPort(hostInfoProto.getPort());
        return host;
    }

    public static List<Device> convertPostDevicesRequestToDevices(List<PostDeviceRequest> devices) {
        return devices
            .parallelStream()
            .map(
                ProtoConverter::convertDeviceProtoRequestToDevice
            )
            .collect(Collectors.toList());
    }

    //DDElements - DeviceDirectoryElements
    public static List<DeviceDirectoryElementProto> convertDDElementsToDDElementsProto(
        List<DeviceDirectoryElement> files
    ) {
        return files.parallelStream()
            .map(ProtoConverter::convertDDElementToDDElementProto)
            .collect(Collectors.toList());
    }

    public static DeviceDirectoryElementProto convertDDElementToDDElementProto(
        DeviceDirectoryElement file
    ) {
        return DeviceDirectoryElementProto.newBuilder()
            .setName(file.name)
            .setIsDirectory(file.isDirectory)
            .setPath(file.path)
            .setSize(file.size)
            .build();
    }

    public static DeviceDirectoryElement convertDDElementProtoToDDElement(
        DeviceDirectoryElementProto file
    ) {
        DeviceDirectoryElement dde = new DeviceDirectoryElement(file.getName(), file.getPath());
        dde.size = file.getSize();
        dde.isDirectory = file.getIsDirectory();
        return dde;
    }

    public static AppDescriptionProto convertAppDescrToAppDescrProto(AppDescription appDescription) {
        return AppDescriptionProto.newBuilder()
            .setAppPackage(appDescription.getAppPackage())
            .setName(appDescription.getName())
            .setAppState(appDescription.getAppState())
            .setIsActive(appDescription.getIsActive())
            .setPath(
                Objects.requireNonNullElse(
                    appDescription.getPath(),
                    ""
                )
            )
            .build();
    }

    public static List<AppDescriptionProto> convertListAppDescrToListAppDescrProto(
        List<AppDescription> appDescriptions
    ) {
        return appDescriptions.parallelStream()
            .map(ProtoConverter::convertAppDescrToAppDescrProto)
            .collect(Collectors.toList());
    }
}
