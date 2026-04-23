package com.finflow.backend.finance.transaction.presentation.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnalyzeTransactionResponse {
    BigDecimal amount;
    String type;
    String suggestedCategoryId;
    String suggestedAccountId;
    String note;
    LocalDateTime transactionDate;
}
