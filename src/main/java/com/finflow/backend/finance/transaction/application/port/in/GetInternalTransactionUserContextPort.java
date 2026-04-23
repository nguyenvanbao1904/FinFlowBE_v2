package com.finflow.backend.finance.transaction.application.port.in;

import com.finflow.backend.finance.transaction.application.dto.InternalTransactionUserContextOutput;
import com.finflow.backend.finance.transaction.application.query.GetInternalTransactionUserContextQuery;

public interface GetInternalTransactionUserContextPort {

    InternalTransactionUserContextOutput execute(GetInternalTransactionUserContextQuery query);
}
