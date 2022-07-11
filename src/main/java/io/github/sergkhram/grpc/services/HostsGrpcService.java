package io.github.sergkhram.grpc.services;

import io.github.sergkhram.data.enums.DeviceType;
import io.github.sergkhram.data.enums.OsType;
import io.github.sergkhram.proto.*;
import io.github.sergkhram.logic.HostRequestsService;
import io.github.sergkhram.data.entity.Host;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.hibernate.LazyInitializationException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class HostsGrpcService extends  HostsServiceGrpc.HostsServiceImplBase {

    @Autowired
    HostRequestsService hostRequestsService;

    @Override
    public void getHostRequest(GetHostRequest request, StreamObserver<GetHostResponse> responseObserver) {
        Host host = hostRequestsService.getHostInfo(request.getId());

        GetHostResponse response = GetHostResponse.newBuilder()
            .setId(String.valueOf(host.getId()))
            .setName(host.getName())
            .setAddress(host.getAddress())
            .setIsActive(host.getIsActive())
            .addAllDevices(
                convertDevices(host)
            )
            .build();

        if(host.getPort()!=null) {
            response = response.toBuilder().setPort(host.getPort()).build();
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private List<DeviceProto> convertDevices(Host host) {
        try {
            return host.getDevices().parallelStream().map(
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
