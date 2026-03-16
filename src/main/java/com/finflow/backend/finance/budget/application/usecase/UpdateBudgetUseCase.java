package com.finflow.backend.finance.budget.application.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.finance.budget.application.mapper.BudgetMapper;
import com.finflow.backend.finance.budget.domain.entity.Budget;
import com.finflow.backend.finance.budget.domain.repository.BudgetRepository;
import com.finflow.backend.finance.budget.exception.BudgetErrorCode;
import com.finflow.backend.finance.budget.presentation.request.UpdateBudgetRequest;
import com.finflow.backend.finance.budget.presentation.response.BudgetResponse;
import com.finflow.backend.finance.transaction.domain.entity.Category;
import com.finflow.backend.finance.transaction.domain.repository.CategoryRepository;
import com.finflow.backend.finance.transaction.exception.TransactionErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateBudgetUseCase {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetMapper budgetMapper;

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public BudgetResponse execute(String userId, UUID budgetId, UpdateBudgetRequest request) {
        log.info("Updating budget {} for user: {}", budgetId, userId);

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new AppException(BudgetErrorCode.BUDGET_NOT_FOUND));

        if (!budget.getUserId().equals(userId)) {
            throw new AppException(BudgetErrorCode.BUDGET_NOT_FOUND);
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new AppException(BudgetErrorCode.BUDGET_INVALID_DATE_RANGE);
        }
        if (request.getEndDate().isBefore(LocalDate.now())) {
            throw new AppException(BudgetErrorCode.BUDGET_END_DATE_IN_PAST);
        }

        Category category = categoryRepository.findByIdAndUserIdOrSystem(request.getCategoryId(), userId)
                .orElseThrow(() -> new AppException(TransactionErrorCode.CATEGORY_NOT_FOUND));

        LocalDate recurringStart = request.getRecurringStartDate();
        if (Boolean.TRUE.equals(request.getIsRecurring()) && recurringStart == null) {
            recurringStart = request.getStartDate();
        }

        budget.setCategory(category);
        budget.setTargetAmount(request.getTargetAmount());
        budget.setStartDate(request.getStartDate());
        budget.setEndDate(request.getEndDate());
        budget.setIsRecurring(request.getIsRecurring());
        budget.setRecurringStartDate(recurringStart);

        Budget updated = budgetRepository.save(budget);
        return budgetMapper.toBudgetResponse(updated);
    }
}

