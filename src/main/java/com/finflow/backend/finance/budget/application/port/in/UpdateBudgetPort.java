package com.finflow.backend.finance.budget.application.port.in;

import com.finflow.backend.finance.budget.application.command.UpdateBudgetCommand;

import com.finflow.backend.common.application.dto.UuidOutput;

public interface UpdateBudgetPort {
    UuidOutput execute(UpdateBudgetCommand command);
}
