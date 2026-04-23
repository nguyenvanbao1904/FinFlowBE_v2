package com.finflow.backend.finance.transaction.application.command;

/**
 * Command for creating a new transaction category.
 */
public record CreateCategoryCommand(
        String userId,
        String name,
        String type,
        String icon,
        String color
) {}
