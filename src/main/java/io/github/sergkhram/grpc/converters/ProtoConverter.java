package io.github.sergkhram.grpc.converters;

import io.github.sergkhram.data.entity.AppDescription;
import io.github.sergkhram.data.entity.Device;
import io.github.sergkhram.data.entity.DeviceDirectoryElement;
import io.github.sergkhram.data.entity.Host;
import io.github.sergkhram.data.enums.DeviceType;
import io.github.sergkhram.data.enums.OsType;
import io.github.sergkhram.proto.*;
import net.badata.protobuf.converter.Configuration;
import net.badata.protobuf.converter.Converter;
import net.badata.protobuf.converter.FieldsIgnore;
import net.badata.protobuf.converter.type.TypeConverter;
import org.hibernate.LazyInitializationException;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
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
        FieldsIgnore ignoredFields = new FieldsIgnore().add(Host.class, "devices");
        Configuration configuration = Configuration.builder().addIgnoredFields(ignoredFields).build();
        HostProto hostProto = Converter.create(configuration).toProtobuf(HostProto.class, host);
        return hostProto
            .toBuilder()
            .setId(
                String.valueOf(host.getId())
            )
            .addAllDevices(
                convertDevicesFromHost(host)
            ).build();
//        HostProto hostObject = HostProto.newBuilder()
//            .setId(String.valueOf(host.getId()))
//            .setName(host.getName())
//            .setAddress(host.getAddress())
//            .setIsActive(host.getIsActive())
//            .addAllDevices(
//                convertDevicesFromHost(host)
//            )
//            .setPort(
//                Objects.requireNonNullElse(
//                    host.getPort(),
//                    0
//                )
//            )
//            .build();
//        return hostObject;
    }

    public static Host convertHostProtoRequestToHost(PostHostRequest hostRequest) {
        FieldsIgnore ignoredFields = new FieldsIgnore().add(Host.class, "id", "devices", "isActive");
        Configuration configuration = Configuration.builder().addIgnoredFields(ignoredFields).build();
        return Converter.create(configuration).toDomain(Host.class, hostRequest);
//        Host host = new Host();
//        host.setName(hostRequest.getName());
//        host.setAddress(hostRequest.getAddress());
//        if(hostRequest.getPort() != 0) host.setPort(hostRequest.getPort());
//        return host;
    }

    public static Host convertUpdateHostProtoRequestToHost(UpdateHostRequest hostRequest) {
        FieldsIgnore ignoredFields = new FieldsIgnore().add(Host.class, "id", "devices", "isActive");
        Configuration configuration = Configuration.builder().addIgnoredFields(ignoredFields).build();
        return Converter.create(configuration).toDomain(Host.class, hostRequest);
//        Host host = new Host();
//        host.setName(hostRequest.getName());
//        host.setAddress(hostRequest.getAddress());
//        if(hostRequest.getPort() != 0) host.setPort(hostRequest.getPort());
//        return host;
    }

    public static List<DeviceProto> convertDevicesFromHost(Host host) {
        try {
            return Objects.requireNonNull(host.getDevices()).parallelStream().map(
                it -> convertDeviceToDeviceProto(it, "host")
//                it -> DeviceProto.newBuilder()
//                    .setDeviceType(
//                        Objects.requireNonNullElse(
//                            it.getDeviceType(), DeviceType.SIMULATOR
//                        ).equals(DeviceType.DEVICE)
//                            ? DeviceTypeProto.DEVICE
//                            : DeviceTypeProto.SIMULATOR
//                    )
//                    .setId(
//                        it.getId() != null
//                            ? String.valueOf(it.getId())
//                            : ""
//                    )
//                    .setName(it.getName())
//                    .setIsActive(it.getIsActive())
//                    .setOsType(
//                        it.getOsType().equals(OsType.ANDROID)
//                            ? OsTypeProto.ANDROID
//                            : OsTypeProto.IOS
//                    )
//                    .setSerial(it.getSerial())
//                    .setState(it.getState())
//                    .setOsVersion(it.getOsVersion())
//                    .build()
            ).collect(Collectors.toList());
        } catch (LazyInitializationException e) {
            return Collections.emptyList();
        }
    }

    public static DeviceProto convertDeviceToDeviceProto(Device device, String... igFields) {
        FieldsIgnore ignoredFields = new FieldsIgnore().add(Device.class, igFields);
        Configuration configuration = Configuration.builder().addIgnoredFields(ignoredFields).build();
        DeviceProto deviceProto = Converter.create(configuration).toProtobuf(DeviceProto.class, device);
        deviceProto = deviceProto.toBuilder().setId(
            device.getId() != null
                ? String.valueOf(device.getId())
                : ""
        ).build();
        return deviceProto;
    }

    public static DeviceProto convertDeviceToDeviceProto(Device device) {
        DeviceProto deviceProto = Converter.create().toProtobuf(DeviceProto.class, device);
        deviceProto = deviceProto.toBuilder().setId(
            device.getId() != null
                ? String.valueOf(device.getId())
                : ""
        ).build();
        return deviceProto;
//        DeviceProto deviceProto = DeviceProto.newBuilder()
//            .setId(
//                device.getId() != null
//                    ? String.valueOf(device.getId())
//                    : ""
//                )
//            .setName(device.getName())
//            .setIsActive(device.getIsActive())
//            .setOsType(
//                device.getOsType().equals(OsType.ANDROID)
//                    ? OsTypeProto.ANDROID
//                    : OsTypeProto.IOS
//            )
//            .setSerial(device.getSerial())
//            .setState(device.getState())
//            .setOsVersion(device.getOsVersion())
//            .setDeviceType(
//                Objects.requireNonNullElse(
//                    device.getDeviceType(), DeviceType.SIMULATOR
//                )
//                    .equals(DeviceType.DEVICE)
//                        ? DeviceTypeProto.DEVICE
//                        : DeviceTypeProto.SIMULATOR
//            )
//            .build();
//        Host host = device.getHost();
//        if(host!=null) {
//            HostInfoProto hostInfoProto = HostInfoProto.newBuilder()
//                .setId(String.valueOf(host.getId()))
//                .setName(host.getName())
//                .setAddress(host.getAddress())
//                .setIsActive(host.getIsActive())
//                .build();
//            if(host.getPort()!=null) hostInfoProto.toBuilder().setPort(host.getPort()).build();
//            deviceProto = deviceProto.toBuilder().setHost(hostInfoProto).build();
//        }
//        return deviceProto;
    }

    public static List<DeviceProto> convertDevicesToProtoDevices(List<Device> devices) {
        return devices.stream()
            .map(
                ProtoConverter::convertDeviceToDeviceProto
            )
            .collect(Collectors.toList());
    }

    public static Device convertDeviceProtoRequestToDevice(PostDeviceRequest deviceRequest) {
        FieldsIgnore ignoredFields = new FieldsIgnore().add(Device.class, "id");
        Configuration configuration = Configuration.builder().addIgnoredFields(ignoredFields).build();
        return Converter.create(configuration).toDomain(Device.class, deviceRequest);
//        Device device = new Device();
//        device.setDeviceType(
//            deviceRequest.getDeviceType().equals(DeviceTypeProto.DEVICE)
//                ? DeviceType.DEVICE
//                : DeviceType.SIMULATOR
//        );
//        device.setName(deviceRequest.getName());
//        device.setOsType(
//            deviceRequest.getOsType().equals(OsTypeProto.ANDROID)
//                ? OsType.ANDROID
//                : OsType.IOS
//        );
//        device.setOsVersion(deviceRequest.getOsVersion());
//        device.setSerial(deviceRequest.getSerial());
//        device.setIsActive(deviceRequest.getIsActive());
//        device.setState(deviceRequest.getState());
//        device.setHost(convertHostInfoProtoToHost(deviceRequest.getHost()));
//        return device;
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

//    public static Host convertHostInfoProtoToHost(HostInfoProto hostInfoProto) {
//        Host host = new Host();
//        host.setName(hostInfoProto.getName());
//        host.setId(UUID.fromString(hostInfoProto.getId()));
//        host.setIsActive(hostInfoProto.getIsActive());
//        host.setAddress(hostInfoProto.getAddress());
//        if(hostInfoProto.getPort()!=0) host.setPort(hostInfoProto.getPort());
//        return host;
//    }

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
        return Converter.create().toProtobuf(DeviceDirectoryElementProto.class, file);
//        return DeviceDirectoryElementProto.newBuilder()
//            .setName(file.name)
//            .setIsDirectory(file.isDirectory)
//            .setPath(file.path)
//            .setSize(file.size)
//            .build();
    }

    public static DeviceDirectoryElement convertDDElementProtoToDDElement(
        DeviceDirectoryElementProto file
    ) {
        return Converter.create().toDomain(DeviceDirectoryElement.class, file);
//        DeviceDirectoryElement dde = new DeviceDirectoryElement(file.getName(), file.getPath());
//        dde.size = file.getSize();
//        dde.isDirectory = file.getIsDirectory();
//        return dde;
    }

    public static AppDescriptionProto convertAppDescrToAppDescrProto(AppDescription appDescription) {
        return Converter.create().toProtobuf(AppDescriptionProto.class, appDescription);
//        AppDescriptionProto adp = AppDescriptionProto.newBuilder()
//            .setAppPackage(appDescription.getAppPackage())
//            .setName(appDescription.getName())
//            .setAppState(appDescription.getAppState())
//            .setIsActive(appDescription.getIsActive())
//            .setPath(
//                Objects.requireNonNullElse(
//                    appDescription.getPath(),
//                    ""
//                )
//            )
//            .build();
//        return adp;
    }

    public static List<AppDescriptionProto> convertListAppDescrToListAppDescrProto(
        List<AppDescription> appDescriptions
    ) {
        return appDescriptions.parallelStream()
            .map(ProtoConverter::convertAppDescrToAppDescrProto)
            .collect(Collectors.toList());
    }

    public static final class OsTypeConverter implements TypeConverter<OsType, OsTypeProto> {
        @Override
        public OsType toDomainValue(Object o) {
            return o.equals(OsTypeProto.ANDROID)
                ? OsType.ANDROID
                : OsType.IOS;
        }

        @Override
        public OsTypeProto toProtobufValue(Object o) {
            return o.equals(OsType.ANDROID)
                    ? OsTypeProto.ANDROID
                    : OsTypeProto.IOS;
        }
    }

    public static final class DeviceTypeConverter implements TypeConverter<DeviceType, DeviceTypeProto> {
        @Override
        public DeviceType toDomainValue(Object o) {
            return o.equals(DeviceTypeProto.DEVICE)
                ? DeviceType.DEVICE
                : DeviceType.SIMULATOR;
        }

        @Override
        public DeviceTypeProto toProtobufValue(Object o) {
            return Objects.requireNonNullElse(
                        o,
                        DeviceType.SIMULATOR
                    ).equals(DeviceType.DEVICE)
                        ? DeviceTypeProto.DEVICE
                        : DeviceTypeProto.SIMULATOR;
        }
    }

    public static final class DeviceHostConverter implements TypeConverter<Host, HostInfoProto> {
        @Override
        public Host toDomainValue(Object o) {
            return (Host) o;
//            FieldsIgnore ignoredFields = new FieldsIgnore().add(Host.class, "devices");
//            Configuration configuration = Configuration.builder().addIgnoredFields(ignoredFields).build();
//            return Converter.create(configuration).toDomain(Host.class, (HostInfoProto) o);
//            HostInfoProto hostProto = (HostInfoProto) o;
//            Host host = new Host();
//            host.setName(hostProto.getName());
//            host.setId(UUID.fromString(hostProto.getId()));
//            host.setIsActive(hostProto.getIsActive());
//            host.setAddress(hostProto.getAddress());
//            if(hostProto.getPort()!=0) host.setPort(hostProto.getPort());
//            return host;
        }

        @Override
        public HostInfoProto toProtobufValue(Object o) {
            return o!=null ? (HostInfoProto) o : null;
//            Host host = (Host) o;
//            if(host!=null) {
//                FieldsIgnore ignoredFields = new FieldsIgnore().add(Host.class, "devices");
//                Configuration configuration = Configuration.builder().addIgnoredFields(ignoredFields).build();
//                HostInfoProto hostInfoProto = Converter.create(configuration).toProtobuf(HostInfoProto.class, host);
//                return hostInfoProto.toBuilder().setId(String.valueOf(host.getId())).build();
////                return HostInfoProto.newBuilder()
////                    .setId(String.valueOf(host.getId()))
////                    .setName(host.getName())
////                    .setAddress(host.getAddress())
////                    .setIsActive(host.getIsActive())
////                    .setPort(
////                        Objects.requireNonNullElse(
////                            host.getPort(),
////                            0
////                        )
////                    )
////                    .build();
//            } else return null;
        }
    }

    public static final class PortConverter implements TypeConverter<Integer, Integer> {
        @Override
        public Integer toDomainValue(Object o) {
            if(!o.equals(0)) {
                return (Integer) o;
            } else return null;
        }

        @Override
        public Integer toProtobufValue(Object o) {
            if(o!=null)
                return (Integer) o;
            else return 0;
        }
    }
}
