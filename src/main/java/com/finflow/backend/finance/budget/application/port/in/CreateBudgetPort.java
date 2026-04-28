package com.finflow.backend.finance.budget.application.port.in;

import com.finflow.backend.finance.budget.application.command.CreateBudgetCommand;
import com.finflow.backend.finance.budget.application.dto.BudgetOutput;

public interface CreateBudgetPort {
    BudgetOutput execute(CreateBudgetCommand command);
}
