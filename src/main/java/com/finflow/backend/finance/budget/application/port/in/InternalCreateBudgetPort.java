package com.finflow.backend.finance.budget.application.port.in;

import com.finflow.backend.finance.budget.application.command.InternalCreateBudgetCommand;

import com.finflow.backend.common.application.dto.UuidOutput;

public interface InternalCreateBudgetPort {
    UuidOutput execute(InternalCreateBudgetCommand command);
}
