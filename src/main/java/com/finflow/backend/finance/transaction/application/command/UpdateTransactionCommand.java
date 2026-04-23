package com.finflow.backend.finance.transaction.application.command;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command for updating an existing transaction.
 */
public record UpdateTransactionCommand(
        String userId,
        UUID transactionId,
        BigDecimal amount,
        String type,
        UUID categoryId,
        UUID accountId,
        String note,
        String transactionDate
) {}
