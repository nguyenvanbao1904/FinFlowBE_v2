package com.finflow.backend.finance.transaction.application.port.in;
import com.finflow.backend.finance.transaction.application.query.GetTransactionSummaryQuery;

import com.finflow.backend.finance.transaction.application.dto.TransactionSummaryOutput;

public interface GetTransactionSummaryPort {

    TransactionSummaryOutput execute(GetTransactionSummaryQuery query);
}
