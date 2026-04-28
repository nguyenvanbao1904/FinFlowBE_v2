package com.finflow.backend.finance.budget.application.port.in;

import com.finflow.backend.finance.budget.application.command.UpdateBudgetCommand;
import com.finflow.backend.finance.budget.application.dto.BudgetOutput;

public interface UpdateBudgetPort {
    BudgetOutput execute(UpdateBudgetCommand command);
}
