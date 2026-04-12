package com.finflow.backend.finance.wealth.application.command;

import java.util.UUID;

/**
 * Command for deleting a wealth account.
 */
public record DeleteWealthAccountCommand(
        String userId,
        UUID accountId
) {}
