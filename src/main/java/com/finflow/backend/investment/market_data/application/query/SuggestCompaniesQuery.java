package com.finflow.backend.investment.market_data.application.query;

public record SuggestCompaniesQuery(
        String query,
        Integer limit
) {}
