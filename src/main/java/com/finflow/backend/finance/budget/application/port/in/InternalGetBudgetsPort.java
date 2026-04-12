package com.finflow.backend.finance.budget.application.port.in;

import com.finflow.backend.finance.budget.domain.entity.Budget;
import java.math.BigDecimal;
import java.util.List;
import com.finflow.backend.finance.budget.presentation.response.BudgetResponse;

public interface InternalGetBudgetsPort {
    List<BudgetResponse> execute(String userId);
}
