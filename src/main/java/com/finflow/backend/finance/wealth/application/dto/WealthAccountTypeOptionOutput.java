package com.finflow.backend.finance.wealth.application.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record WealthAccountTypeOptionOutput(
        UUID id,
        String code,
        String displayName,
        String icon,
        String color,
        Boolean transactionEligible,
        Boolean debt,
        String group
) {}
