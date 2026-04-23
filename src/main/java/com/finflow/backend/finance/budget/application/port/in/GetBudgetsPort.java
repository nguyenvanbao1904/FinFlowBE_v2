package com.finflow.backend.finance.budget.application.port.in;
import com.finflow.backend.finance.budget.application.query.GetBudgetsQuery;
import com.finflow.backend.finance.budget.application.dto.BudgetOutput;

import java.util.List;

public interface GetBudgetsPort {
    List<BudgetOutput> execute(GetBudgetsQuery query);
}
