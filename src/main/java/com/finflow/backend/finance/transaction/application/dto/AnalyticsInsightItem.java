package com.finflow.backend.finance.transaction.application.dto;

public record AnalyticsInsightItem(
        String id,
        String type,
        String title,
        String message,
        double confidence
) {}
