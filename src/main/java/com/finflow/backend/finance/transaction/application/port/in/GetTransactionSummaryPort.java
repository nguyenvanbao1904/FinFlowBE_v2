package com.finflow.backend.finance.transaction.application.port.in;

import com.finflow.backend.finance.transaction.presentation.response.TransactionSummaryResponse;

public interface GetTransactionSummaryPort {

    TransactionSummaryResponse execute(String userId);
}
