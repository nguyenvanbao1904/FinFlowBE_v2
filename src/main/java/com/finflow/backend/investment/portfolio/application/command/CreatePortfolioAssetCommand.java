package com.finflow.backend.investment.portfolio.application.command;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command for adding an asset to a portfolio.
 */
public record CreatePortfolioAssetCommand(
        String userId,
        UUID portfolioId,
        String symbol,
        BigDecimal quantity,
        BigDecimal averagePrice
) {}
