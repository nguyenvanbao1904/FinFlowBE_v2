package com.finflow.backend.finance.transaction.application.port.in;

import com.finflow.backend.finance.transaction.application.command.UpdateCategoryCommand;

import com.finflow.backend.common.application.dto.UuidOutput;

public interface UpdateCategoryPort {
    UuidOutput execute(UpdateCategoryCommand command);
}
