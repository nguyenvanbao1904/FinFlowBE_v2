package com.finflow.backend.investment.market_data.application.query;

public record GetInvestmentDividendsQuery(
        String symbol,
        Integer annualLimit
) {}
