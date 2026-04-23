package com.finflow.backend.finance.budget.application.port.in;
import com.finflow.backend.finance.budget.application.query.InternalGetBudgetsQuery;

import java.util.List;
import com.finflow.backend.finance.budget.application.dto.BudgetOutput;

public interface InternalGetBudgetsPort {
    List<BudgetOutput> execute(InternalGetBudgetsQuery query);
}
