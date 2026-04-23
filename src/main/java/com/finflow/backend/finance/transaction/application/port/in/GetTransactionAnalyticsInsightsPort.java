package com.finflow.backend.finance.transaction.application.port.in;
import com.finflow.backend.finance.transaction.application.query.GetTransactionAnalyticsInsightsQuery;

import com.finflow.backend.finance.transaction.application.dto.AnalyticsInsightsOutput;

public interface GetTransactionAnalyticsInsightsPort {

    AnalyticsInsightsOutput execute(GetTransactionAnalyticsInsightsQuery query);
}
