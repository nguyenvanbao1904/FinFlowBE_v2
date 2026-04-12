package com.finflow.backend.finance.transaction.application.result;

/**
 * Application-layer result for a single AI analytics insight.
 * Framework-free; mapped to HTTP response by the web adapter.
 */
public record AnalyticsInsightItem(
        String id,
        String type,
        String title,
        String message,
        double confidence
) {}
