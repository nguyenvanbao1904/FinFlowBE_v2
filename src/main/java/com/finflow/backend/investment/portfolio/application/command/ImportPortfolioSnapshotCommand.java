package com.finflow.backend.investment.portfolio.application.command;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Command for importing a portfolio snapshot (replaces current holdings with given state).
 */
public record ImportPortfolioSnapshotCommand(
        String userId,
        UUID portfolioId,
        BigDecimal cashBalance,
        List<HoldingSnapshot> holdings
) {
    public record HoldingSnapshot(
            String symbol,
            BigDecimal totalQuantity,
            BigDecimal averagePrice
    ) {}
}
