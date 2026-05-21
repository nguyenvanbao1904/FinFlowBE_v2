package com.finflow.backend.investment.portfolio.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateTradeTransactionCommand(
        String userId,
        UUID portfolioId,
        String tradeType,
        String symbol,
        BigDecimal quantity,
        BigDecimal price,
        BigDecimal amount,
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal feePercent,
        BigDecimal taxPercent,
        String transactionDate
) {}
