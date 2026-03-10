package com.finflow.backend.transaction.presentation.request;

import com.finflow.backend.transaction.domain.enums.CategoryType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateTransactionRequest {

    @NotNull(message = "UNCATEGORIZED_EXCEPTION")
    @DecimalMin(value = "0.01", message = "INVALID_AMOUNT")
    BigDecimal amount;

    @NotNull(message = "UNCATEGORIZED_EXCEPTION")
    CategoryType type;

    @NotNull(message = "UNCATEGORIZED_EXCEPTION")
    UUID categoryId;

    String note;

    @NotBlank(message = "UNCATEGORIZED_EXCEPTION")
    String transactionDate;  // ISO8601 format with timezone: 2026-03-05T18:09:41.830+07:00
}
