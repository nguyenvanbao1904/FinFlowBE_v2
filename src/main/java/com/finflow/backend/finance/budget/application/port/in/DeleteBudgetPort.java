package com.finflow.backend.finance.budget.application.port.in;

import com.finflow.backend.finance.budget.application.command.DeleteBudgetCommand;

public interface DeleteBudgetPort {
    void execute(DeleteBudgetCommand command);
}
