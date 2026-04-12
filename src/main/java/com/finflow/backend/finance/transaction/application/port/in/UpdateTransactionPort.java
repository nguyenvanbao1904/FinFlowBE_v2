package com.finflow.backend.finance.transaction.application.port.in;

import com.finflow.backend.finance.transaction.application.command.UpdateTransactionCommand;
import com.finflow.backend.finance.transaction.presentation.response.TransactionResponse;

public interface UpdateTransactionPort {
    TransactionResponse execute(UpdateTransactionCommand command);
}
