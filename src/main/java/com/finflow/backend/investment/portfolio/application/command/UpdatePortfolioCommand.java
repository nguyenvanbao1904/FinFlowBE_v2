package com.finflow.backend.investment.portfolio.application.command;

import java.util.UUID;

public record UpdatePortfolioCommand(
        String userId,
        UUID portfolioId,
        String name
) {}
