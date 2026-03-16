package com.finflow.backend.finance.budget.presentation.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateBudgetRequest {

    @NotNull(message = "CATEGORY_NOT_FOUND")
    UUID categoryId;

    @NotNull(message = "BUDGET_INVALID_DATE_RANGE")
    @Positive(message = "BUDGET_INVALID_DATE_RANGE")
    BigDecimal targetAmount;

    @NotNull(message = "BUDGET_INVALID_DATE_RANGE")
    LocalDate startDate;

    @NotNull(message = "BUDGET_INVALID_DATE_RANGE")
    LocalDate endDate;

    Boolean isRecurring;

    LocalDate recurringStartDate;
}

