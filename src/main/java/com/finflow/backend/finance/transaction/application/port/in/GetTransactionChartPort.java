package com.finflow.backend.finance.transaction.application.port.in;
import com.finflow.backend.finance.transaction.application.query.GetTransactionChartQuery;

import com.finflow.backend.finance.transaction.application.dto.TransactionChartOutput;

public interface GetTransactionChartPort {

    TransactionChartOutput execute(GetTransactionChartQuery query);
}
