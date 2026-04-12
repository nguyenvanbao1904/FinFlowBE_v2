package com.finflow.backend.finance.budget.application.port.in;

import com.finflow.backend.finance.budget.application.command.DeleteBudgetCommand;
import com.finflow.backend.finance.budget.domain.entity.Budget;
import java.util.UUID;

public interface DeleteBudgetPort {
    void execute(DeleteBudgetCommand command);
}
