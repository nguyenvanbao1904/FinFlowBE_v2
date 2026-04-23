package com.finflow.backend.finance.transaction.application.port.in;

import com.finflow.backend.finance.transaction.application.command.AddTransactionCommand;

import com.finflow.backend.common.application.dto.UuidOutput;

public interface AddTransactionPort {
    UuidOutput execute(AddTransactionCommand command);
}
