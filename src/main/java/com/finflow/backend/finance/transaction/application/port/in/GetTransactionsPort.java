package com.finflow.backend.finance.transaction.application.port.in;

import com.finflow.backend.finance.transaction.application.dto.TransactionPageOutput;
import com.finflow.backend.finance.transaction.application.query.GetTransactionsQuery;

public interface GetTransactionsPort {
    TransactionPageOutput execute(GetTransactionsQuery query);
}
