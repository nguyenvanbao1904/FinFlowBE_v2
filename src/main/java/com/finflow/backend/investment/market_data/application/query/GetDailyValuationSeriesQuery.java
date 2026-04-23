package com.finflow.backend.investment.market_data.application.query;

public record GetDailyValuationSeriesQuery(
        String symbol,
        String startDate,
        String endDate
) {}
