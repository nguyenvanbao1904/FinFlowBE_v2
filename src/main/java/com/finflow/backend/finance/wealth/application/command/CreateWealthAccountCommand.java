package com.finflow.backend.finance.wealth.application.command;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command for creating a new wealth account.
 */
public record CreateWealthAccountCommand(
        String userId,
        String name,
        UUID accountTypeId,
        BigDecimal balance,
        Boolean includeInNetWorth
) {}
