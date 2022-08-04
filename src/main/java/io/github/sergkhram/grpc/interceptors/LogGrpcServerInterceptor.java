package io.github.sergkhram.grpc.interceptors;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogGrpcServerInterceptor implements ServerInterceptor {
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        ServerCall<ReqT, RespT> listener = new ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(serverCall) {

            @Override
            public void sendMessage(RespT message) {
                log.info("Sending message to clients: {}",  message);
                super.sendMessage(message);
            }
        };

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(serverCallHandler.startCall(listener, metadata)) {

            @Override
            public void onMessage(ReqT message) {
                log.info("Received message from clients: {}", message);
                super.onMessage(message);
            }
        };
    }
}
