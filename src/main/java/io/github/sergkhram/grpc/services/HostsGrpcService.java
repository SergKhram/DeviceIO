package io.github.sergkhram.grpc.services;

import com.google.protobuf.Empty;
import io.github.sergkhram.proto.*;
import io.github.sergkhram.logic.HostRequestsService;
import io.github.sergkhram.data.entity.Host;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static io.github.sergkhram.grpc.converters.ProtoConverter.*;

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

            Empty response = Empty.getDefaultInstance();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}
