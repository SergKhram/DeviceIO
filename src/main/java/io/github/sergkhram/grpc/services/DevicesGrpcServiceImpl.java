package io.github.sergkhram.grpc.services;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.github.sergkhram.data.entity.AppDescription;
import io.github.sergkhram.data.entity.Device;
import io.github.sergkhram.data.entity.DeviceDirectoryElement;
import io.github.sergkhram.data.enums.IOSPackageType;
import io.github.sergkhram.data.enums.OsType;
import io.github.sergkhram.data.service.DownloadService;
import io.github.sergkhram.logic.DeviceRequestsService;
import io.github.sergkhram.proto.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import static io.github.sergkhram.grpc.converters.ProtoConverter.*;
import static io.github.sergkhram.utils.grpc.ErrorUtil.prepareGrpcError;

@GrpcService
@Slf4j
public class DevicesGrpcServiceImpl extends DevicesServiceGrpc.DevicesServiceImplBase {

    @Autowired
    DeviceRequestsService deviceRequestsService;

    @Override
    public void getDeviceRequest(DeviceId request, StreamObserver<DeviceProto> responseObserver) {
        try {
            Device device = deviceRequestsService.getDeviceInfo(request.getId());

            DeviceProto response = convertDeviceToDeviceProto(device);

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchElementException | IllegalArgumentException e) {
            responseObserver.onError(
                prepareGrpcError(e)
            );
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
                .addAllDevices(convertDevicesToProtoDevices(devices))
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                prepareGrpcError(e)
            );
        }
    }

    @Override
    public void postDeviceRequest(PostDeviceRequest request, StreamObserver<DeviceProto> responseObserver) {
        try {
            Device device = convertDeviceProtoRequestToDevice(request);
            Device savedDevice = deviceRequestsService.saveDevice(device);

            DeviceProto response = convertDeviceToDeviceProto(savedDevice);

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                prepareGrpcError(e)
            );
        }
    }

    @Override
    public void updateDeviceRequest(UpdateDeviceRequest request, StreamObserver<DeviceProto> responseObserver) {
        try {
            Device device = convertUpdateDeviceProtoRequestToDevice(request);
            device.setId(UUID.fromString(request.getId()));
            Device savedDevice = deviceRequestsService.saveDevice(device);

            DeviceProto response = convertDeviceToDeviceProto(savedDevice);

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                prepareGrpcError(e)
            );
        }
    }

    @Override
    public void deleteDeviceRequest(DeviceId request, StreamObserver<Empty> responseObserver) {
        try {
            Device device = deviceRequestsService.getDeviceInfo(request.getId());
            deviceRequestsService.deleteDevice(device);

            Empty response = Empty.getDefaultInstance();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                prepareGrpcError(e)
            );
        }
    }

    @Override
    public void postDevicesRequest(PostDevicesRequest request, StreamObserver<Empty> responseObserver) {
        try {
            List<Device> devices = convertPostDevicesRequestToDevices(request.getDevicesList());
            deviceRequestsService.saveDevices(devices);

            Empty response = Empty.getDefaultInstance();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                prepareGrpcError(e)
            );
        }
    }

    @Override
    public void postDeviceRebootRequest(DeviceId request, StreamObserver<Empty> responseObserver) {
        try {
            Device device = deviceRequestsService.getDeviceInfo(request.getId());
            deviceRequestsService.reboot(device);

            Empty response = Empty.getDefaultInstance();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                prepareGrpcError(e)
            );
        }
    }

    @Override
    public void getDevicesStatesRequest(Empty request, StreamObserver<DevicesStatesResponse> responseObserver) {
        try {
            Map<String, String> states = deviceRequestsService.getDevicesStates();

            DevicesStatesResponse response = DevicesStatesResponse.newBuilder()
                .putAllStates(states)
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                prepareGrpcError(e)
            );
        }
    }

    @Override
    public void postExecuteShellRequest(
        ExecuteShellRequest request,
        StreamObserver<ExecuteShellResponse> responseObserver
    ) {
        try {
            Device device = deviceRequestsService.getDeviceInfo(request.getId());
            if (device.getOsType().equals(OsType.IOS)) {
                responseObserver.onError(
                    Status.CANCELLED
                        .withDescription("Execute shell request allowed for ANDROID only")
                        .asRuntimeException()
                );
            } else {
                String result = deviceRequestsService.executeShell(device, request.getBody());

                ExecuteShellResponse response = ExecuteShellResponse.newBuilder()
                    .setResult(result)
                    .build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        } catch (Exception e) {
            responseObserver.onError(
                prepareGrpcError(e)
            );
        }
    }

    @Override
    public void getFilesListOfDeviceRequest(
        GetDeviceFilesRequest request,
        StreamObserver<GetDeviceFilesResponse> responseObserver
    ) {
        try {
            IOSPackageType iosPackageType = IOSPackageType.APPLICATION;
            Device device = deviceRequestsService.getDeviceInfo(request.getId());
            request.getIosPackageType();
            if(!request.getIosPackageType().isEmpty())
                iosPackageType = IOSPackageType.valueOf(request.getIosPackageType().toUpperCase());
            List<DeviceDirectoryElement> files = deviceRequestsService.getListFiles(device, request.getPath(), iosPackageType);
            GetDeviceFilesResponse response = GetDeviceFilesResponse.newBuilder()
                .addAllFiles(convertDDElementsToDDElementsProto(files))
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                prepareGrpcError(e)
            );
        }
    }

    @Override
    public void postDownloadFileRequest(
        FileDownloadRequest request,
        StreamObserver<DataChunk> responseObserver
    ) {
        File currentFile = null;
        try {
            IOSPackageType iosPackageType = IOSPackageType.APPLICATION;
            if(!request.getIosPackageType().isEmpty())
                iosPackageType = IOSPackageType.valueOf(request.getIosPackageType().toUpperCase());
            DownloadService.DownloadRequestData requestData = DownloadService.DownloadRequestData
                .builder()
                .device(deviceRequestsService.getDeviceInfo(request.getId()))
                .deviceDirectoryElement(convertDDElementProtoToDDElement(request.getDeviceDirectoryElementProto()))
                .iosPackageType(iosPackageType)
                .build();
            DownloadService.DownloadResponseData responseData = deviceRequestsService.download(requestData);
            currentFile = responseData.getFile();
            responseObserver.onNext(
                DataChunk
                    .newBuilder()
                    .setData(
                        ByteString.copyFrom(
                            FileUtils.readFileToByteArray(currentFile)
                        )
                    )
                    .build()
            );

            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                prepareGrpcError(e)
            );

        } finally {
            if (currentFile != null && currentFile.exists()) {
                FileUtils.deleteQuietly(currentFile);
            }
        }
    }

    @Override
    public void getAppsListRequest(
        DeviceId request,
        StreamObserver<GetAppsResponse> responseObserver
    ) {
        try {
            Device device = deviceRequestsService.getDeviceInfo(request.getId());
            List<AppDescription> apps = deviceRequestsService.getAppsList(device);
            GetAppsResponse response = GetAppsResponse.newBuilder()
                .addAllApps(convertListAppDescrToListAppDescrProto(apps))
                .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(
                prepareGrpcError(e)
            );
        }
    }
}
