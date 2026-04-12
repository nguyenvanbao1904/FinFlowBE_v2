package com.finflow.backend.finance.budget.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Command for creating a new budget.
 */
public record CreateBudgetCommand(
        String userId,
        UUID categoryId,
        BigDecimal targetAmount,
        LocalDate startDate,
        LocalDate endDate,
        Boolean isRecurring,
        LocalDate recurringStartDate
) {}
