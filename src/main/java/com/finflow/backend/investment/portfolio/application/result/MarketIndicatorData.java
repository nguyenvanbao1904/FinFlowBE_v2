package com.finflow.backend.investment.portfolio.application.result;

import java.math.BigDecimal;

/**
 * Application-layer data carrier for financial valuation indicators.
 * Replaces direct use of {@code market_data.domain.entity.FinancialIndicator}
 * inside the portfolio bounded context.
 */
public record MarketIndicatorData(
        String companyId,
        int year,
        int quarter,
        BigDecimal pe,
        BigDecimal pb,
        BigDecimal ps,
        BigDecimal roe,
        BigDecimal roa
) {}
