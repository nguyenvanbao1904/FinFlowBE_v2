package com.finflow.backend.finance.budget.application.usecase;

import com.finflow.backend.finance.budget.application.command.InternalCreateBudgetCommand;
import com.finflow.backend.finance.budget.application.port.in.InternalCreateBudgetPort;
import com.finflow.backend.finance.transaction.api.TransactionCategoryReadApi;

import com.finflow.backend.common.application.dto.UuidOutput;
import com.finflow.backend.common.exception.AppException;

import com.finflow.backend.finance.budget.domain.entity.Budget;
import com.finflow.backend.finance.budget.domain.repository.BudgetRepository;
import com.finflow.backend.finance.budget.exception.BudgetErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Internal create for AI agent — no {@code @PreAuthorize}; secured by {@code X-Internal-Api-Key}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InternalCreateBudgetUseCase implements InternalCreateBudgetPort {

    private final BudgetRepository budgetRepository;
    private final TransactionCategoryReadApi transactionCategoryReadApi;
    

    @Transactional
    @Override
    public UuidOutput execute(InternalCreateBudgetCommand command) {
        String userId = command.userId();
        log.info("[INTERNAL] Creating budget for userId: {}", userId);

        if (command.startDate().isAfter(command.endDate())) {
            throw new AppException(BudgetErrorCode.BUDGET_INVALID_DATE_RANGE);
        }
        if (command.endDate().isBefore(LocalDate.now())) {
            throw new AppException(BudgetErrorCode.BUDGET_END_DATE_IN_PAST);
        }

        if (!transactionCategoryReadApi.isExpenseCategoryOfUserOrSystem(command.categoryId(), userId)) {
            throw new AppException(BudgetErrorCode.BUDGET_CATEGORY_MUST_BE_EXPENSE);
        }

        LocalDate recurringStart = command.recurringStartDate();
        if (Boolean.TRUE.equals(command.isRecurring()) && recurringStart == null) {
            recurringStart = command.startDate();
        }

        Budget budget = Budget.builder()
                .userId(userId)
                .categoryId(command.categoryId())
                .targetAmount(command.targetAmount())
                .startDate(command.startDate())
                .endDate(command.endDate())
                .isRecurring(command.isRecurring())
                .recurringStartDate(recurringStart)
                .build();

        Budget saved = budgetRepository.save(budget);
        return new UuidOutput(saved.getId());
    }
}
