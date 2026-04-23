package com.finflow.backend.investment.market_data.application.query;

public record GetInvestmentFinancialSeriesQuery(
        String symbol,
        Integer annualLimit,
        Integer quarterlyLimit
) {}
