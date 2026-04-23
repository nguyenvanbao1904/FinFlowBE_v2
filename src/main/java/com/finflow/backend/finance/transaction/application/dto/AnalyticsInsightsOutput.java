package com.finflow.backend.finance.transaction.application.dto;

import java.util.List;

public record AnalyticsInsightsOutput(
        List<AnalyticsInsightItem> insights,
        boolean cached
) {}
