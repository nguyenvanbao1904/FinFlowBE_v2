package com.finflow.backend.finance.wealth.application.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record WealthAccountOutput(
        UUID id,
        String name,
        WealthAccountTypeOptionOutput wealthAccountType,
        BigDecimal balance,
        Boolean isSynced,
        Boolean includeInNetWorth
) {}
