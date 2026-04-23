package com.finflow.backend.investment.portfolio.application.query;

public record GetPortfolioAssetsQuery(
        String userId,
        java.util.UUID portfolioId
) {}
