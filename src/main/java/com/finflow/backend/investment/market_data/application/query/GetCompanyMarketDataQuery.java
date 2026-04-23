package com.finflow.backend.investment.market_data.application.query;

import java.util.List;

public record GetCompanyMarketDataQuery(
        String symbol,
        List<String> includes,
        Integer annualLimit,
        Integer quarterlyLimit
) {}
