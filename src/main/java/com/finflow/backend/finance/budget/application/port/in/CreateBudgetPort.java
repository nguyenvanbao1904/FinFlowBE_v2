package com.finflow.backend.finance.budget.application.port.in;

import com.finflow.backend.finance.budget.application.command.CreateBudgetCommand;

import com.finflow.backend.common.application.dto.UuidOutput;

public interface CreateBudgetPort {
    UuidOutput execute(CreateBudgetCommand command);
}
