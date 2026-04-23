package com.finflow.backend.finance.budget.presentation.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BudgetResponse {

    UUID id;
    BudgetCategoryResponse category;
    BigDecimal targetAmount;
    /** Sum of expense transactions in this category between startDate and endDate (inclusive). */
    BigDecimal spentAmount;
    LocalDate startDate;
    LocalDate endDate;
    Boolean isRecurring;
    LocalDate recurringStartDate;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}

