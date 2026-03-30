package com.finflow.backend.finance.budget.application.usecase;

import com.finflow.backend.finance.budget.application.mapper.BudgetMapper;
import com.finflow.backend.finance.budget.domain.entity.Budget;
import com.finflow.backend.finance.budget.domain.repository.BudgetRepository;
import com.finflow.backend.finance.budget.presentation.response.BudgetResponse;
import com.finflow.backend.finance.transaction.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetBudgetsUseCase {

    private final BudgetRepository budgetRepository;
    private final BudgetMapper budgetMapper;
    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<BudgetResponse> execute(String userId) {
        log.info("Getting budgets for user: {}", userId);

        List<Budget> budgets = budgetRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return budgets.stream()
                .map(b -> {
                    BudgetResponse r = budgetMapper.toBudgetResponse(b);
                    BigDecimal spent = transactionRepository.sumExpenseByUserIdAndCategoryIdAndTransactionDateBetween(
                            b.getUserId(),
                            b.getCategory().getId(),
                            b.getStartDate().atStartOfDay(),
                            b.getEndDate().plusDays(1).atStartOfDay());
                    r.setSpentAmount(spent != null ? spent : BigDecimal.ZERO);
                    return r;
                })
                .toList();
    }
}

