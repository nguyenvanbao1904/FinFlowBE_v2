package com.finflow.backend.finance.transaction.application.dto;

import com.finflow.backend.finance.common.enums.CategoryType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record TransactionOutput(
        UUID id,
        BigDecimal amount,
        CategoryType type,
        CategoryOutput category,
        String note,
        UUID accountId,
        LocalDateTime transactionDate,
        LocalDateTime createdAt
) {}
