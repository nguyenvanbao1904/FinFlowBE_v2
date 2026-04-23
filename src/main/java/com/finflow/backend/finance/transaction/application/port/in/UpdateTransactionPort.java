package com.finflow.backend.finance.transaction.application.port.in;

import com.finflow.backend.finance.transaction.application.command.UpdateTransactionCommand;

import com.finflow.backend.common.application.dto.UuidOutput;

public interface UpdateTransactionPort {
    UuidOutput execute(UpdateTransactionCommand command);
}
