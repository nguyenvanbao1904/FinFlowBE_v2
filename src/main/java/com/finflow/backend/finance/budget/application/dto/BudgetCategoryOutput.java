package com.finflow.backend.finance.budget.application.dto;

import com.finflow.backend.finance.common.enums.CategoryType;
import lombok.Builder;

import java.util.UUID;

@Builder
public record BudgetCategoryOutput(
        UUID id,
        String name,
        CategoryType type,
        String icon,
        String color,
        boolean systemCategory
) {}
