package com.finflow.backend.finance.budget.application.port.in;

import com.finflow.backend.finance.budget.presentation.request.CreateBudgetRequest;
import com.finflow.backend.finance.transaction.domain.enums.CategoryType;
import com.finflow.backend.finance.budget.presentation.response.BudgetResponse;
import com.finflow.backend.finance.budget.domain.entity.Budget;
import java.time.LocalDate;
import com.finflow.backend.finance.transaction.domain.entity.Category;

public interface InternalCreateBudgetPort {
    BudgetResponse execute(String userId, CreateBudgetRequest request);
}
