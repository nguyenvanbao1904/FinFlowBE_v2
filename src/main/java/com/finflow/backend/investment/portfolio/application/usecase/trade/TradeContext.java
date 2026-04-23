package com.finflow.backend.investment.portfolio.application.usecase.trade;

import com.finflow.backend.investment.portfolio.application.command.CreateTradeTransactionCommand;
import com.finflow.backend.investment.portfolio.domain.entity.Portfolio;

import java.math.BigDecimal;

/**
 * Immutable context object passed to each {@link TradeHandler}.
 * Carries the pre-validated command plus the resolved portfolio and
 * the already-computed fee/tax percentages.
 */
public record TradeContext(
        CreateTradeTransactionCommand command,
        Portfolio portfolio,
        BigDecimal feePercent,
        BigDecimal taxPercent
) {}
