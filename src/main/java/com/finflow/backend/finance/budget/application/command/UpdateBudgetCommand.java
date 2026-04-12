package com.finflow.backend.finance.budget.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Command for updating an existing budget.
 */
public record UpdateBudgetCommand(
        String userId,
        UUID budgetId,
        UUID categoryId,
        BigDecimal targetAmount,
        LocalDate startDate,
        LocalDate endDate,
        Boolean isRecurring,
        LocalDate recurringStartDate
) {}
