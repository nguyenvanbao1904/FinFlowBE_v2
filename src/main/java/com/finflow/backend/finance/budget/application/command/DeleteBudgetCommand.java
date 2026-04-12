package com.finflow.backend.finance.budget.application.command;

import java.util.UUID;

/**
 * Command for deleting a budget.
 */
public record DeleteBudgetCommand(
        String userId,
        UUID budgetId
) {}
