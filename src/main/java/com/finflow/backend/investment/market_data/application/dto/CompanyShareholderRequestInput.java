package com.finflow.backend.investment.market_data.application.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CompanyShareholderRequestInput(
        String shareholderName,
        BigDecimal shareOwnPercent,
        BigDecimal shareOwnAmount
) {}
