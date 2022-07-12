package io.github.sergkhram.utils.grpc;

import io.grpc.StatusRuntimeException;

public class ErrorUtil {
    public static StatusRuntimeException prepareGrpcError(Throwable e) {
        return io.grpc.Status.fromThrowable(e).withDescription(e.getLocalizedMessage()).asRuntimeException();
    }
}
