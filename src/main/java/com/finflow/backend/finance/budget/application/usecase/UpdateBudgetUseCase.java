package com.finflow.backend.finance.budget.application.usecase;

import com.finflow.backend.finance.budget.application.port.in.UpdateBudgetPort;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.common.exception.CommonErrorCode;
import com.finflow.backend.finance.budget.application.mapper.BudgetMapper;
import com.finflow.backend.finance.budget.domain.entity.Budget;
import com.finflow.backend.finance.budget.domain.repository.BudgetRepository;
import com.finflow.backend.finance.budget.exception.BudgetErrorCode;
import com.finflow.backend.finance.budget.application.command.UpdateBudgetCommand;
import com.finflow.backend.finance.budget.presentation.response.BudgetResponse;
import com.finflow.backend.finance.transaction.domain.entity.Category;
import com.finflow.backend.finance.transaction.domain.repository.CategoryRepository;
import com.finflow.backend.finance.transaction.exception.TransactionErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateBudgetUseCase implements UpdateBudgetPort {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetMapper budgetMapper;

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Override
    public BudgetResponse execute(UpdateBudgetCommand command) {
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

        Category category = categoryRepository.findByIdAndUserIdOrSystem(command.categoryId(), userId)
                .orElseThrow(() -> new AppException(TransactionErrorCode.CATEGORY_NOT_FOUND));

        budget.setCategory(category);
        budget.setTargetAmount(command.targetAmount());
        budget.setStartDate(command.startDate());
        budget.setEndDate(command.endDate());
        budget.setIsRecurring(command.isRecurring() != null ? command.isRecurring() : false);
        budget.setRecurringStartDate(command.recurringStartDate());

        Budget updated = budgetRepository.save(budget);
        return budgetMapper.toBudgetResponse(updated);
    }
}

