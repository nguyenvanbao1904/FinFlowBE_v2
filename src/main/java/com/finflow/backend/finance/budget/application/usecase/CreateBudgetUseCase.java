package com.finflow.backend.finance.budget.application.usecase;

import com.finflow.backend.finance.budget.application.port.in.CreateBudgetPort;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.finance.budget.application.mapper.BudgetMapper;
import com.finflow.backend.finance.budget.domain.entity.Budget;
import com.finflow.backend.finance.budget.domain.repository.BudgetRepository;
import com.finflow.backend.finance.budget.exception.BudgetErrorCode;
import com.finflow.backend.finance.budget.application.command.CreateBudgetCommand;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class CreateBudgetUseCase implements CreateBudgetPort {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetMapper budgetMapper;

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Override
    public BudgetResponse execute(CreateBudgetCommand command) {
        String userId = command.userId();
        log.info("Creating budget for userId: {}", userId);

        // Validate date range
        if (command.startDate().isAfter(command.endDate())) {
            throw new AppException(BudgetErrorCode.BUDGET_INVALID_DATE_RANGE);
        }

        // Validate category ownership
        Category category = categoryRepository.findByIdAndUserIdOrSystem(command.categoryId(), userId)
                .orElseThrow(() -> new AppException(TransactionErrorCode.CATEGORY_NOT_FOUND));

        // Create budget entity
        Budget budget = Budget.builder()
                .userId(userId)
                .category(category)
                .targetAmount(command.targetAmount())
                .startDate(command.startDate())
                .endDate(command.endDate())
                .isRecurring(command.isRecurring() != null ? command.isRecurring() : false)
                .recurringStartDate(command.recurringStartDate())
                .build();

        Budget saved = budgetRepository.save(budget);
        return budgetMapper.toBudgetResponse(saved);
    }
}

