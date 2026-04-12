package com.finflow.backend.finance.transaction.application.command;

import java.util.UUID;

/**
 * Command for deleting a transaction category.
 */
public record DeleteCategoryCommand(
        String userId,
        UUID categoryId
) {}
