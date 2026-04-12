package com.finflow.backend.finance.budget.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.finflow.backend.finance.budget.presentation.response.BudgetResponse;
import com.finflow.backend.finance.budget.domain.entity.Budget;

public interface GetBudgetsPort {
    List<BudgetResponse> execute(String userId);
}
