package com.finflow.backend.investment.market_data.application.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record BankFinancialIndicatorRequestInput(
        String companyId,
        Integer year,
        Integer quarter,
        BigDecimal pe,
        BigDecimal pb,
        BigDecimal ps,
        BigDecimal roe,
        BigDecimal roa,
        BigDecimal eps,
        BigDecimal bvps,
        BigDecimal cplh
) {}
