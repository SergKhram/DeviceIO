package io.github.sergkhram.grpc;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogGrpcInterceptor implements ClientInterceptor {
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(methodDescriptor, callOptions.withoutWaitForReady())) {
            public void sendMessage(ReqT message) {
                log.info(
                    "sent by grpc " + methodDescriptor.getFullMethodName() + " : " + message
                );
                super.sendMessage(message);
            }
            public void start(final ClientCall.Listener<RespT> responseListener, final Metadata headers) {
                ClientCall.Listener<RespT> listener = new ForwardingClientCallListener<RespT>() {
                    @Override
                    protected Listener<RespT> delegate() {
                        return responseListener;
                    }

                    public void onMessage(RespT message) {
                        log.info("received by grpc" + methodDescriptor.getFullMethodName() + ": " + message);
                        super.onMessage(message);
                    }
                };
                super.start(listener, headers);
            }
        };
    }
}
