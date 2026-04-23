package com.finflow.backend.finance.transaction.application.port.in;
import com.finflow.backend.finance.transaction.application.query.AnalyzeTransactionQuery;
import com.finflow.backend.finance.transaction.application.dto.AnalyzeTransactionOutput;

public interface AnalyzeTransactionPort {

    AnalyzeTransactionOutput execute(AnalyzeTransactionQuery query);
}
