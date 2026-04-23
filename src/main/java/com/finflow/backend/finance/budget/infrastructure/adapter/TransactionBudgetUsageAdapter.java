package com.finflow.backend.finance.budget.infrastructure.adapter;

import com.finflow.backend.finance.budget.domain.repository.BudgetRepository;
import com.finflow.backend.finance.budget.api.BudgetReadApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adapter owned by budget module to expose budget usage data
 * for transaction use cases through budget's public read API contract.
 */
@Component
@RequiredArgsConstructor
public class TransactionBudgetUsageAdapter implements BudgetReadApi {

    private final BudgetRepository budgetRepository;

    @Override
    public long countBudgetsByCategoryId(UUID categoryId) {
        return budgetRepository.countByCategoryId(categoryId);
    }
}
