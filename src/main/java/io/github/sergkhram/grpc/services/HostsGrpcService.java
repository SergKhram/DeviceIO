package io.github.sergkhram.grpc.services;

import io.github.sergkhram.data.enums.DeviceType;
import io.github.sergkhram.data.enums.OsType;
import io.github.sergkhram.proto.*;
import io.github.sergkhram.logic.HostRequestsService;
import io.github.sergkhram.data.entity.Host;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.hibernate.LazyInitializationException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@GrpcService
@Slf4j
public class HostsGrpcService extends  HostsServiceGrpc.HostsServiceImplBase {

    @Autowired
    HostRequestsService hostRequestsService;

    @Override
    public void getHostRequest(GetHostRequest request, StreamObserver<HostProto> responseObserver) {
        Host host = hostRequestsService.getHostInfo(request.getId());

        HostProto response = convertHost(host);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getHostsListRequest(GetHostsListRequest request, StreamObserver<GetHostsListResponse> responseObserver) {
        List<Host> hosts = hostRequestsService.getHostsList(request.getStringFilter());

        GetHostsListResponse response = GetHostsListResponse.newBuilder()
            .addAllHosts(convertHosts(hosts))
            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private List<HostProto> convertHosts(List<Host> hosts) {
        return hosts.parallelStream()
            .map(
                this::convertHost
            )
            .collect(Collectors.toList());
    }

    private HostProto convertHost(Host host) {
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

    private List<DeviceProto> convertDevices(Host host) {
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
