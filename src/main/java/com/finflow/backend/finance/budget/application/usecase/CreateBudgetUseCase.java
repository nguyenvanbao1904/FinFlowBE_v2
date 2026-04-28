package com.finflow.backend.finance.budget.application.usecase;

import com.finflow.backend.finance.budget.application.port.in.CreateBudgetPort;
import com.finflow.backend.finance.transaction.api.TransactionCategoryReadApi;

import com.finflow.backend.common.exception.AppException;

import com.finflow.backend.finance.budget.application.dto.BudgetCategoryOutput;
import com.finflow.backend.finance.budget.application.dto.BudgetOutput;
import com.finflow.backend.finance.budget.application.mapper.BudgetMapper;
import com.finflow.backend.finance.budget.domain.entity.Budget;
import com.finflow.backend.finance.budget.domain.repository.BudgetRepository;
import com.finflow.backend.finance.budget.exception.BudgetErrorCode;
import com.finflow.backend.finance.budget.application.command.CreateBudgetCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreateBudgetUseCase implements CreateBudgetPort {

    private final BudgetRepository budgetRepository;
    private final TransactionCategoryReadApi transactionCategoryReadApi;
    private final BudgetMapper budgetMapper;

    @Transactional
    @Override
    public BudgetOutput execute(CreateBudgetCommand command) {
        String userId = command.userId();
        log.info("Creating budget for userId: {}", userId);

        // Validate date range
        if (command.startDate().isAfter(command.endDate())) {
            throw new AppException(BudgetErrorCode.BUDGET_INVALID_DATE_RANGE);
        }

        if (!transactionCategoryReadApi.isExpenseCategoryOfUserOrSystem(command.categoryId(), userId)) {
            throw new AppException(BudgetErrorCode.BUDGET_CATEGORY_NOT_FOUND);
        }

        // Create budget entity
        Budget budget = Budget.builder()
                .userId(userId)
                .categoryId(command.categoryId())
                .targetAmount(command.targetAmount())
                .startDate(command.startDate())
                .endDate(command.endDate())
                .isRecurring(command.isRecurring() != null ? command.isRecurring() : false)
                .recurringStartDate(command.recurringStartDate())
                .build();

        Budget saved = budgetRepository.save(budget);

        // Enrich with category info
        TransactionCategoryReadApi.CategorySnapshot cat =
                transactionCategoryReadApi.findCategory(saved.getCategoryId());
        BudgetCategoryOutput categoryOutput = cat != null
                ? BudgetCategoryOutput.builder()
                        .id(cat.id()).name(cat.name()).type(cat.type())
                        .icon(cat.icon()).color(cat.color()).systemCategory(cat.systemCategory())
                        .build()
                : null;

        BudgetOutput base = budgetMapper.toBudgetOutput(saved);
        return BudgetOutput.builder()
                .id(base.id())
                .category(categoryOutput)
                .targetAmount(base.targetAmount())
                .spentAmount(BigDecimal.ZERO)
                .startDate(base.startDate())
                .endDate(base.endDate())
                .isRecurring(base.isRecurring())
                .recurringStartDate(base.recurringStartDate())
                .createdAt(base.createdAt())
                .updatedAt(base.updatedAt())
                .build();
    }
}

