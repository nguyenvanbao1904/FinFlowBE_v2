package com.finflow.backend.investment.market_data.application.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CashFlowStatementRequestInput(
        String companyId,
        Integer year,
        Integer quarter,
        BigDecimal operatingCashflow,
        BigDecimal investingCashflow,
        BigDecimal financingCashflow
) {}
