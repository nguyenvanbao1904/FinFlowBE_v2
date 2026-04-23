package com.finflow.backend.investment.market_data.application.query;

import java.util.List;

public record GetCompanyIndustriesQuery(
        List<String> symbols
) {}
