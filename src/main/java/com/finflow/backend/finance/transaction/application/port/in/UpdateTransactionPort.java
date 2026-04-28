package com.finflow.backend.finance.transaction.application.port.in;

import com.finflow.backend.finance.transaction.application.command.UpdateTransactionCommand;
import com.finflow.backend.finance.transaction.application.dto.TransactionOutput;

public interface UpdateTransactionPort {
    TransactionOutput execute(UpdateTransactionCommand command);
}
