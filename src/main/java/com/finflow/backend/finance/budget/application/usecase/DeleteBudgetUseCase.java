package com.finflow.backend.finance.budget.application.usecase;

import com.finflow.backend.finance.budget.application.port.in.DeleteBudgetPort;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.finance.budget.application.command.DeleteBudgetCommand;
import com.finflow.backend.finance.budget.domain.entity.Budget;
import com.finflow.backend.finance.budget.domain.repository.BudgetRepository;
import com.finflow.backend.finance.budget.exception.BudgetErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeleteBudgetUseCase implements DeleteBudgetPort {

    private final BudgetRepository budgetRepository;

    @Transactional
    @Override
    public void execute(DeleteBudgetCommand command) {
        String userId = command.userId();
        UUID budgetId = command.budgetId();
        log.info("Deleting budget {} for user: {}", budgetId, userId);

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new AppException(BudgetErrorCode.BUDGET_NOT_FOUND));

        if (!budget.getUserId().equals(userId)) {
            throw new AppException(BudgetErrorCode.BUDGET_NOT_FOUND);
        }

        budgetRepository.delete(budget);
    }
}

