package com.finflow.backend.finance.transaction.application.port.in;

import com.finflow.backend.finance.transaction.application.TransactionChartRange;
import com.finflow.backend.finance.transaction.presentation.response.TransactionChartResponse;

import java.time.LocalDate;

public interface GetTransactionChartPort {

    TransactionChartResponse execute(String userId, TransactionChartRange range, LocalDate referenceDate);
}
