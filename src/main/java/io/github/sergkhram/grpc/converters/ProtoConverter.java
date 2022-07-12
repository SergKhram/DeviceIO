package io.github.sergkhram.grpc.converters;

import io.github.sergkhram.data.entity.Host;
import io.github.sergkhram.data.enums.DeviceType;
import io.github.sergkhram.data.enums.OsType;
import io.github.sergkhram.proto.*;
import org.hibernate.LazyInitializationException;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProtoConverter {
    public static List<HostProto> convertHosts(List<Host> hosts) {
        return hosts.parallelStream()
            .map(
                ProtoConverter::convertHostToHostProto
            )
            .collect(Collectors.toList());
    }

    public static HostProto convertHostToHostProto(Host host) {
        HostProto hostObject = HostProto.newBuilder()
            .setId(String.valueOf(host.getId()))
            .setName(host.getName())
            .setAddress(host.getAddress())
            .setIsActive(host.getIsActive())
            .addAllDevices(
                convertDevices(host)
            )
            .build();
        if(host.getPort()!=null) {
            hostObject = hostObject.toBuilder().setPort(
                host.getPort()
            ).build();
        }
        return hostObject;
    }

    public static Host convertHostProtoRequestToHost(PostHostRequest hostRequest) {
        Host host = new Host();
        host.setName(hostRequest.getName());
        host.setAddress(hostRequest.getAddress());
        if(hostRequest.getPort() != 0) host.setPort(hostRequest.getPort());
        return host;
    }

    public static Host convertUpdateOrDeleteHostProtoRequestToHost(UpdateHostRequest hostRequest) {
        Host host = new Host();
        host.setName(hostRequest.getName());
        host.setAddress(hostRequest.getAddress());
        if(hostRequest.getPort() != 0) host.setPort(hostRequest.getPort());
        return host;
    }

    public static List<DeviceProto> convertDevices(Host host) {
        try {
            return Objects.requireNonNull(host.getDevices()).parallelStream().map(
                it -> DeviceProto.newBuilder()
                    .setDeviceType(
                        it.getDeviceType().equals(DeviceType.DEVICE)
                            ? DeviceTypeProto.DEVICE
                            : DeviceTypeProto.SIMULATOR
                    )
                    .setId(String.valueOf(it.getId()))
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
        } catch (LazyInitializationException e) {
            return Collections.emptyList();
        }
    }
}
