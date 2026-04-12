package com.finflow.backend.finance.wealth.application.command;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command for updating an existing wealth account.
 */
public record UpdateWealthAccountCommand(
        String userId,
        UUID accountId,
        String name,
        UUID accountTypeId,
        BigDecimal balance,
        Boolean includeInNetWorth
) {}
