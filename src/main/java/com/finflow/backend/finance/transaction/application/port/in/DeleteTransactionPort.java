package com.finflow.backend.finance.transaction.application.port.in;

import com.finflow.backend.finance.transaction.application.command.DeleteTransactionCommand;

public interface DeleteTransactionPort {
    void execute(DeleteTransactionCommand command);
}
