package com.finflow.backend.investment.portfolio.application.query;

public record GetPortfolioInsightsQuery(
        String userId,
        String portfolioId
) {
}
