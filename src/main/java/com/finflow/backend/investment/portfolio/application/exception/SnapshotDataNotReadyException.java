package com.finflow.backend.investment.portfolio.application.exception;

/**
 * Báo hiệu dữ liệu thị trường (VNINDEX) chưa sẵn sàng — dùng với {@code @Retryable} backoff.
 */
public class SnapshotDataNotReadyException extends RuntimeException {

    public SnapshotDataNotReadyException(String message) {
        super(message);
    }
}
