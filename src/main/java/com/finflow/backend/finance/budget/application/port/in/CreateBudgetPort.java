package com.finflow.backend.finance.budget.application.port.in;

import com.finflow.backend.finance.budget.application.command.CreateBudgetCommand;
import com.finflow.backend.finance.budget.presentation.response.BudgetResponse;
import java.time.LocalDate;
import com.finflow.backend.finance.budget.domain.entity.Budget;
import com.finflow.backend.finance.transaction.domain.entity.Category;

public interface CreateBudgetPort {
    BudgetResponse execute(CreateBudgetCommand command);
}
