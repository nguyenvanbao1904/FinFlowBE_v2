package com.finflow.backend.finance.transaction.application.result;

import java.util.List;

/**
 * Application-layer result returned by {@code GetTransactionAnalyticsInsightsUseCase}.
 * Framework-free; mapped to HTTP response by the web adapter.
 */
public record AnalyticsInsightsResult(
        List<AnalyticsInsightItem> insights,
        boolean cached
) {}
