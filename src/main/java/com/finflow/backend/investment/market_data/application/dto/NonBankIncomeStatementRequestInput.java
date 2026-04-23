package com.finflow.backend.investment.market_data.application.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record NonBankIncomeStatementRequestInput(
        String companyId,
        Integer year,
        Integer quarter,
        BigDecimal netRevenue,
        BigDecimal totalRevenue,
        BigDecimal profitAfterTax,
        BigDecimal grossMargin,
        BigDecimal netMargin
) {}
