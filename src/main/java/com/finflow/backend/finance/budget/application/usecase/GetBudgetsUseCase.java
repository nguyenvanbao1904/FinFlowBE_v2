package com.finflow.backend.finance.budget.application.usecase;

import com.finflow.backend.finance.budget.application.port.in.GetBudgetsPort;
import com.finflow.backend.finance.budget.application.query.GetBudgetsQuery;
import com.finflow.backend.finance.transaction.api.TransactionCategoryReadApi;
import com.finflow.backend.finance.transaction.api.TransactionReadApi;

import com.finflow.backend.finance.budget.application.dto.BudgetCategoryOutput;
import com.finflow.backend.finance.budget.application.dto.BudgetOutput;
import com.finflow.backend.finance.budget.application.mapper.BudgetMapper;
import com.finflow.backend.finance.budget.domain.entity.Budget;
import com.finflow.backend.finance.budget.domain.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetBudgetsUseCase implements GetBudgetsPort {

    private final BudgetRepository budgetRepository;
    private final BudgetMapper budgetMapper;
    private final TransactionCategoryReadApi transactionCategoryReadApi;
    private final TransactionReadApi transactionReadApi;

    @Transactional(readOnly = true)
    @Override
    public List<BudgetOutput> execute(GetBudgetsQuery request) {
        String userId = request.userId();
        log.info("Getting budgets for user: {}", userId);

        List<Budget> budgets = budgetRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (budgets.isEmpty()) {
            return List.of();
        }

        // Batch 1: load all categories in one query
        Set<UUID> categoryIds = budgets.stream()
                .map(Budget::getCategoryId)
                .collect(Collectors.toSet());
        Map<UUID, TransactionCategoryReadApi.CategorySnapshot> categoryMap =
                transactionCategoryReadApi.findCategoriesByIds(categoryIds);

        // Batch 2: load all relevant expense transactions in one query,
        // spanning the union of all budget date windows
        LocalDateTime minStart = budgets.stream()
                .map(b -> b.getStartDate().atStartOfDay())
                .min(LocalDateTime::compareTo)
                .orElseThrow();
        LocalDateTime maxEnd = budgets.stream()
                .map(b -> b.getEndDate().plusDays(1).atStartOfDay())
                .max(LocalDateTime::compareTo)
                .orElseThrow();

        List<TransactionReadApi.ExpenseRow> expenseRows =
                transactionReadApi.findExpensesByUserIdAndCategoryIdsBetween(
                        userId, categoryIds, minStart, maxEnd);

        // Assemble results — per-budget date filtering happens here in Java
        return budgets.stream()
                .map(b -> {
                    BudgetOutput r = budgetMapper.toBudgetOutput(b);
                    BudgetCategoryOutput category =
                            toBudgetCategoryOutput(categoryMap.get(b.getCategoryId()));

                    LocalDateTime budgetStart = b.getStartDate().atStartOfDay();
                    LocalDateTime budgetEnd = b.getEndDate().plusDays(1).atStartOfDay();
                    BigDecimal spent = expenseRows.stream()
                            .filter(row -> b.getCategoryId().equals(row.categoryId())
                                    && !row.transactionDate().isBefore(budgetStart)
                                    && row.transactionDate().isBefore(budgetEnd))
                            .map(TransactionReadApi.ExpenseRow::amount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return BudgetOutput.builder()
                            .id(r.id())
                            .category(category)
                            .targetAmount(r.targetAmount())
                            .spentAmount(spent)
                            .startDate(r.startDate())
                            .endDate(r.endDate())
                            .isRecurring(r.isRecurring())
                            .recurringStartDate(r.recurringStartDate())
                            .createdAt(r.createdAt())
                            .updatedAt(r.updatedAt())
                            .build();
                })
                .toList();
    }

    private BudgetCategoryOutput toBudgetCategoryOutput(TransactionCategoryReadApi.CategorySnapshot category) {
        if (category == null) {
            return null;
        }
        return BudgetCategoryOutput.builder()
                .id(category.id())
                .name(category.name())
                .type(category.type())
                .icon(category.icon())
                .color(category.color())
                .systemCategory(category.systemCategory())
                .build();
    }
}

