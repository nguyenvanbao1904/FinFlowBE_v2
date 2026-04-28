package com.finflow.backend.investment.portfolio.application.command;

import java.util.UUID;

public record DeletePortfolioCommand(
        String userId,
        UUID portfolioId
) {}
