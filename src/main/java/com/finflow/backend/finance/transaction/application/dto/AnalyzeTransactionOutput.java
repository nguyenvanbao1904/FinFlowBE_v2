package com.finflow.backend.finance.transaction.application.dto;

import com.finflow.backend.finance.common.enums.CategoryType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record AnalyzeTransactionOutput(
        BigDecimal amount,
        CategoryType type,
        String suggestedCategoryId,
        String suggestedAccountId,
        String note,
        LocalDateTime transactionDate
) {}
