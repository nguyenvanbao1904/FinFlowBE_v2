package com.finflow.backend.investment.portfolio.application.query;

import java.util.UUID;

public record GetPortfolioHealthQuery(
        String userId,
        UUID portfolioId,
        int quartersLimit
) {}
