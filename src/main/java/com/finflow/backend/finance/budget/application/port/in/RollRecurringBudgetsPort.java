package com.finflow.backend.finance.budget.application.port.in;
import com.finflow.backend.finance.budget.application.command.RollRecurringBudgetsCommand;

public interface RollRecurringBudgetsPort {
    void execute(RollRecurringBudgetsCommand command);
}
