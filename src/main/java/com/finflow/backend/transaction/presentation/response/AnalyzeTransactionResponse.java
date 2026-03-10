package com.finflow.backend.transaction.presentation.response;

import com.finflow.backend.transaction.domain.enums.CategoryType;
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
    CategoryType type;
    String suggestedCategoryId;
    String note;
    LocalDateTime transactionDate;
}
