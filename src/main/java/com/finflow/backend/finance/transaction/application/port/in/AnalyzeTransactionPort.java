package com.finflow.backend.finance.transaction.application.port.in;

import com.finflow.backend.finance.transaction.application.command.AnalyzeTransactionCommand;
import com.finflow.backend.finance.transaction.presentation.response.AnalyzeTransactionResponse;

public interface AnalyzeTransactionPort {

    AnalyzeTransactionResponse execute(String userId, AnalyzeTransactionCommand command);
}
