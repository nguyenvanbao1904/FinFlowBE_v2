package com.finflow.backend.finance.transaction.application.command;

import java.util.UUID;

/**
 * Command for updating an existing transaction category.
 */
public record UpdateCategoryCommand(
        String userId,
        UUID categoryId,
        String name,
        String icon,
        String color
) {}
