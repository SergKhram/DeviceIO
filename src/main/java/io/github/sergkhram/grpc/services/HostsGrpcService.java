package io.github.sergkhram.grpc.services;

import com.google.protobuf.Empty;
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

import java.util.*;
import java.util.stream.Collectors;

@GrpcService
@Slf4j
public class HostsGrpcService extends  HostsServiceGrpc.HostsServiceImplBase {

    @Autowired
    HostRequestsService hostRequestsService;

    @Override
    public void getHostRequest(GetHostRequest request, StreamObserver<HostProto> responseObserver) {
        try {
            Host host = hostRequestsService.getHostInfo(request.getId());

            HostProto response = convertHostToHostProto(host);

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchElementException |IllegalArgumentException e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void getHostsListRequest(GetHostsListRequest request, StreamObserver<GetHostsListResponse> responseObserver) {
        try {
            List<Host> hosts = hostRequestsService.getHostsList(request.getStringFilter());

            GetHostsListResponse response = GetHostsListResponse.newBuilder()
                .addAllHosts(convertHosts(hosts))
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void postHostRequest(PostHostRequest request, StreamObserver<HostProto> responseObserver) {
        try {
            Host savedHost = hostRequestsService.saveHost(
                convertHostProtoRequestToHost(request)
            );

            HostProto response = convertHostToHostProto(savedHost);

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void updateHostRequest(UpdateOrDeleteHostRequest request, StreamObserver<HostProto> responseObserver) {
        try {
            Host host = convertUpdateOrDeleteHostProtoRequestToHost(request);
            host.setId(UUID.fromString(request.getId()));
            Host savedHost = hostRequestsService.saveHost(host);

            HostProto response = convertHostToHostProto(savedHost);

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void deleteHostRequest(UpdateOrDeleteHostRequest request, StreamObserver<Empty> responseObserver) {
        try {
            Host host = hostRequestsService.getHostInfo(request.getId());
            hostRequestsService.deleteHost(host);

            Empty response = Empty.newBuilder().build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    private List<HostProto> convertHosts(List<Host> hosts) {
        return hosts.parallelStream()
            .map(
                this::convertHostToHostProto
            )
            .collect(Collectors.toList());
    }

    private HostProto convertHostToHostProto(Host host) {
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

    private Host convertHostProtoRequestToHost(PostHostRequest hostRequest) {
        Host host = new Host();
        host.setName(hostRequest.getName());
        host.setAddress(hostRequest.getAddress());
        if(hostRequest.getPort() != 0) host.setPort(hostRequest.getPort());
        return host;
    }

    private Host convertUpdateOrDeleteHostProtoRequestToHost(UpdateOrDeleteHostRequest hostRequest) {
        Host host = new Host();
        host.setName(hostRequest.getName());
        host.setAddress(hostRequest.getAddress());
        if(hostRequest.getPort() != 0) host.setPort(hostRequest.getPort());
        return host;
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
