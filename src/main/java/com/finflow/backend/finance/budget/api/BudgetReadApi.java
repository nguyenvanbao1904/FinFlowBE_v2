package com.finflow.backend.finance.budget.api;

import java.util.UUID;

/**
 * Public read contract exposed by budget submodule for other finance submodules.
 */
public interface BudgetReadApi {

    long countBudgetsByCategoryId(UUID categoryId);
}
