package com.finflow.backend.investment.portfolio.application.query;

import java.util.UUID;

public record GetPortfolioVsMarketQuery(
        String userId,
        UUID portfolioId,
        String benchmarkCode
) {}
