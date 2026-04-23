package com.finflow.backend.finance.transaction.application.result;

import java.math.BigDecimal;

/**
 * Parsed result returned by the AI transaction prefill service.
 * Application-layer model used as a data transfer between the AI adapter and the use case.
 * All fields are nullable since the AI may omit any of them.
 */
public record TransactionPrefillResult(
        BigDecimal amount,
        String type,            // "INCOME" | "EXPENSE" | "SAVING" — nullable
        String categoryId,      // UUID string — nullable
        String accountId,       // UUID string — nullable
        String note,            // nullable
        String transactionDate  // ISO-8601 string — nullable
) {}
