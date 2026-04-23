package com.finflow.backend.finance.budget.application.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record BudgetOutput(
        UUID id,
        BudgetCategoryOutput category,
        BigDecimal targetAmount,
        BigDecimal spentAmount,
        LocalDate startDate,
        LocalDate endDate,
        Boolean isRecurring,
        LocalDate recurringStartDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
