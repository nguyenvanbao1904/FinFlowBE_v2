package com.finflow.backend.finance.transaction.application.port.in;

import com.finflow.backend.finance.transaction.application.command.AddTransactionCommand;
import com.finflow.backend.finance.transaction.application.dto.TransactionOutput;

public interface AddTransactionPort {
    TransactionOutput execute(AddTransactionCommand command);
}
