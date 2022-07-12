package io.github.sergkhram.grpc.services;

import io.github.sergkhram.data.entity.Device;
import io.github.sergkhram.logic.DeviceRequestsService;
import io.github.sergkhram.logic.HostRequestsService;
import io.github.sergkhram.proto.*;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.NoSuchElementException;

import static io.github.sergkhram.grpc.converters.ProtoConverter.*;

@GrpcService
@Slf4j
public class DevicesGrpcService extends DevicesServiceGrpc.DevicesServiceImplBase {

    @Autowired
    HostRequestsService hostRequestsService;

    @Autowired
    DeviceRequestsService deviceRequestsService;

    @Override
    public void getDeviceRequest(DeviceId request, StreamObserver<DeviceProto> responseObserver) {
        try {
            Device device = deviceRequestsService.getDeviceInfo(request.getId());

            DeviceProto response = convertDeviceToDeviceProto(device);

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchElementException |IllegalArgumentException e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void getDevicesListRequest(GetDevicesListRequest request, StreamObserver<GetDevicesListResponse> responseObserver) {
        try {
            List<Device> devices = request.getIsSaved()
                ? deviceRequestsService.getDBDevicesList(
                    request.getStringFilter(),
                    request.getHostId()
                )
                : deviceRequestsService.getCurrentDevicesList(
                    request.getHostId()
            );

            GetDevicesListResponse response = GetDevicesListResponse.newBuilder()
                .addAllDevices(convertDevices(devices))
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchElementException |IllegalArgumentException e) {
            responseObserver.onError(e);
        }
    }
}
