package com.finflow.backend.finance.transaction.application.port.in;

import com.finflow.backend.finance.transaction.application.command.CreateCategoryCommand;

import com.finflow.backend.common.application.dto.UuidOutput;

public interface CreateCategoryPort {
    UuidOutput execute(CreateCategoryCommand command);
}
