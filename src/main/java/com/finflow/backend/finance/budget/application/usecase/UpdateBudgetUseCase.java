package com.finflow.backend.finance.budget.application.usecase;

import com.finflow.backend.finance.budget.application.port.in.UpdateBudgetPort;
import com.finflow.backend.finance.transaction.api.TransactionCategoryReadApi;
import com.finflow.backend.finance.transaction.api.TransactionReadApi;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.common.exception.CommonErrorCode;

import com.finflow.backend.finance.budget.application.dto.BudgetCategoryOutput;
import com.finflow.backend.finance.budget.application.dto.BudgetOutput;
import com.finflow.backend.finance.budget.application.mapper.BudgetMapper;
import com.finflow.backend.finance.budget.domain.entity.Budget;
import com.finflow.backend.finance.budget.domain.repository.BudgetRepository;
import com.finflow.backend.finance.budget.exception.BudgetErrorCode;
import com.finflow.backend.finance.budget.application.command.UpdateBudgetCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateBudgetUseCase implements UpdateBudgetPort {

    private final BudgetRepository budgetRepository;
    private final TransactionCategoryReadApi transactionCategoryReadApi;
    private final TransactionReadApi transactionReadApi;
    private final BudgetMapper budgetMapper;

    @Transactional
    @Override
    public BudgetOutput execute(UpdateBudgetCommand command) {
        String userId = command.userId();
        UUID budgetId = command.budgetId();
        log.info("Updating budget {} for userId: {}", budgetId, userId);

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new AppException(BudgetErrorCode.BUDGET_NOT_FOUND));

        if (!budget.getUserId().equals(userId)) {
            log.warn("User {} attempted to update budget {} owned by {}",
                    userId, budgetId, budget.getUserId());
            throw new AppException(CommonErrorCode.UNAUTHORIZED);
        }

        if (command.startDate().isAfter(command.endDate())) {
            throw new AppException(BudgetErrorCode.BUDGET_INVALID_DATE_RANGE);
        }

        if (!transactionCategoryReadApi.isExpenseCategoryOfUserOrSystem(command.categoryId(), userId)) {
            throw new AppException(BudgetErrorCode.BUDGET_CATEGORY_NOT_FOUND);
        }

        budget.setCategoryId(command.categoryId());
        budget.setTargetAmount(command.targetAmount());
        budget.setStartDate(command.startDate());
        budget.setEndDate(command.endDate());
        budget.setIsRecurring(command.isRecurring() != null ? command.isRecurring() : false);
        budget.setRecurringStartDate(command.recurringStartDate());

        Budget updated = budgetRepository.save(budget);

        // Enrich with category info
        TransactionCategoryReadApi.CategorySnapshot cat =
                transactionCategoryReadApi.findCategory(updated.getCategoryId());
        BudgetCategoryOutput categoryOutput = cat != null
                ? BudgetCategoryOutput.builder()
                        .id(cat.id()).name(cat.name()).type(cat.type())
                        .icon(cat.icon()).color(cat.color()).systemCategory(cat.systemCategory())
                        .build()
                : null;

        // Compute spent amount for the budget period
        LocalDateTime budgetStart = updated.getStartDate().atStartOfDay();
        LocalDateTime budgetEnd = updated.getEndDate().plusDays(1).atStartOfDay();
        List<TransactionReadApi.ExpenseRow> expenseRows =
                transactionReadApi.findExpensesByUserIdAndCategoryIdsBetween(
                        userId, Set.of(updated.getCategoryId()), budgetStart, budgetEnd);
        BigDecimal spent = expenseRows.stream()
                .map(TransactionReadApi.ExpenseRow::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BudgetOutput base = budgetMapper.toBudgetOutput(updated);
        return BudgetOutput.builder()
                .id(base.id())
                .category(categoryOutput)
                .targetAmount(base.targetAmount())
                .spentAmount(spent)
                .startDate(base.startDate())
                .endDate(base.endDate())
                .isRecurring(base.isRecurring())
                .recurringStartDate(base.recurringStartDate())
                .createdAt(base.createdAt())
                .updatedAt(base.updatedAt())
                .build();
    }
}

