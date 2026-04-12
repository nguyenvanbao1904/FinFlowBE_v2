package com.finflow.backend.finance.transaction.application.port.in;

import com.finflow.backend.finance.transaction.application.result.AnalyticsInsightsResult;

public interface GetTransactionAnalyticsInsightsPort {

    AnalyticsInsightsResult execute(String userId);
}
