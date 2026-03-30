package com.finflow.backend.notification;

import org.slf4j.MDC;

/**
 * Binds {@code correlationId} for logging in async notification work; clears MDC in {@code finally}.
 */
public final class MdcCorrelationSupport {

    private static final String CORRELATION_ID = "correlationId";

    private MdcCorrelationSupport() {}

    public static void run(String correlationId, Runnable action) {
        if (correlationId != null && !correlationId.isBlank()) {
            MDC.put(CORRELATION_ID, correlationId);
        }
        try {
            action.run();
        } finally {
            MDC.remove(CORRELATION_ID);
        }
    }
}
