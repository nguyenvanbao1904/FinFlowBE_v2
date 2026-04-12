package com.finflow.backend.investment.portfolio.application.command;

import com.finflow.backend.investment.portfolio.domain.entity.TradeType;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command for recording a trade transaction in a portfolio.
 */
public record CreateTradeTransactionCommand(
        String userId,
        UUID portfolioId,
        TradeType tradeType,
        String symbol,
        BigDecimal quantity,
        BigDecimal price,
        BigDecimal amount,
        BigDecimal feePercent,
        BigDecimal taxPercent,
        String transactionDate
) {}
