package com.finflow.backend.investment.market_data.application.query;

public record GetInvestmentFullAnalysisQuery(
        String symbol,
        Integer annualLimit,
        Integer quarterlyLimit
) {}
