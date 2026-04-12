package com.finflow.backend.finance.transaction.application.command;

import java.util.UUID;

/**
 * Command for deleting a transaction.
 */
public record DeleteTransactionCommand(
        String userId,
        UUID transactionId
) {}
