package com.finflow.backend.finance.budget.application.usecase;

import com.finflow.backend.finance.budget.application.port.in.InternalCreateBudgetPort;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.finance.budget.application.mapper.BudgetMapper;
import com.finflow.backend.finance.budget.domain.entity.Budget;
import com.finflow.backend.finance.budget.domain.repository.BudgetRepository;
import com.finflow.backend.finance.budget.exception.BudgetErrorCode;
import com.finflow.backend.finance.budget.presentation.request.CreateBudgetRequest;
import com.finflow.backend.finance.budget.presentation.response.BudgetResponse;
import com.finflow.backend.finance.transaction.domain.entity.Category;
import com.finflow.backend.finance.transaction.domain.enums.CategoryType;
import com.finflow.backend.finance.transaction.domain.repository.CategoryRepository;
import com.finflow.backend.finance.transaction.exception.TransactionErrorCode;
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
    private final CategoryRepository categoryRepository;
    private final BudgetMapper budgetMapper;

    @Transactional
    @Override
    public BudgetResponse execute(String userId, CreateBudgetRequest request) {
        log.info("[INTERNAL] Creating budget for userId: {}", userId);

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new AppException(BudgetErrorCode.BUDGET_INVALID_DATE_RANGE);
        }
        if (request.getEndDate().isBefore(LocalDate.now())) {
            throw new AppException(BudgetErrorCode.BUDGET_END_DATE_IN_PAST);
        }

        Category category = categoryRepository.findByIdAndUserIdOrSystem(request.getCategoryId(), userId)
                .orElseThrow(() -> new AppException(TransactionErrorCode.CATEGORY_NOT_FOUND));

        if (category.getType() != CategoryType.EXPENSE) {
            throw new AppException(BudgetErrorCode.BUDGET_CATEGORY_MUST_BE_EXPENSE);
        }

        LocalDate recurringStart = request.getRecurringStartDate();
        if (Boolean.TRUE.equals(request.getIsRecurring()) && recurringStart == null) {
            recurringStart = request.getStartDate();
        }

        Budget budget = Budget.builder()
                .userId(userId)
                .category(category)
                .targetAmount(request.getTargetAmount())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isRecurring(request.getIsRecurring())
                .recurringStartDate(recurringStart)
                .build();

        Budget saved = budgetRepository.save(budget);
        return budgetMapper.toBudgetResponse(saved);
    }
}
