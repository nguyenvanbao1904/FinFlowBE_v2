package com.finflow.backend.finance.transaction.application.port.in;

import com.finflow.backend.finance.transaction.application.command.AddTransactionCommand;
import com.finflow.backend.finance.transaction.presentation.response.TransactionResponse;

public interface AddTransactionPort {
    TransactionResponse execute(AddTransactionCommand command);
}
