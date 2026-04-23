package com.finflow.backend.investment.market_data.application.query;

public record GetInvestmentValuationsQuery(
        String symbol,
        Integer annualLimit,
        String startDate,
        String endDate,
        Boolean showQuarterly
) {}
