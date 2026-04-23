package com.finflow.backend.finance.transaction.application.command;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command for adding a new transaction.
 */
public record AddTransactionCommand(
        String userId,
        BigDecimal amount,
        String type,
        UUID categoryId,
        UUID accountId,
        String note,
        String transactionDate
) {}
