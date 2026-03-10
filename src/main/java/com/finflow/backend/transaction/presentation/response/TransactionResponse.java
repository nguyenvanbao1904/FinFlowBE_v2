package com.finflow.backend.transaction.presentation.response;

import com.finflow.backend.transaction.domain.enums.CategoryType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionResponse {
    UUID id;
    BigDecimal amount;
    CategoryType type;
    CategoryResponse category;
    String note;
    LocalDateTime transactionDate;
    LocalDateTime createdAt;
}
