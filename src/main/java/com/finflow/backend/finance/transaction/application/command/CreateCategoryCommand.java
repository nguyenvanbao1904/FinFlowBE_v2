package com.finflow.backend.finance.transaction.application.command;

import com.finflow.backend.finance.transaction.domain.enums.CategoryType;

/**
 * Command for creating a new transaction category.
 */
public record CreateCategoryCommand(
        String userId,
        String name,
        CategoryType type,
        String icon,
        String color
) {}
