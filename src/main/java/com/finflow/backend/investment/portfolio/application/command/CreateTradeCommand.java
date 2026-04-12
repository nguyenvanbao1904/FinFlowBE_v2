package com.finflow.backend.investment.portfolio.application.command;

import com.finflow.backend.investment.portfolio.domain.entity.TradeType;

import java.math.BigDecimal;

/**
 * Application-layer command for creating a trade transaction.
 * Replaces direct use of {@code CreateTradeTransactionRequest} (presentation) in the use case.
 * The controller maps the HTTP request to this command before calling the use case.
 */
public record CreateTradeCommand(
        TradeType tradeType,

        /** BUY / SELL / DIVIDEND */
        String symbol,
        BigDecimal quantity,
        BigDecimal price,

        /** DEPOSIT / WITHDRAW */
        BigDecimal amount,

        /** Fee percentage, e.g. 0.1 means 0.1%. Defaults to 0 if null. */
        BigDecimal feePercent,

        /** Tax percentage. Defaults to 0 (or 0.1 for SELL) if null. */
        BigDecimal taxPercent,

        /**
         * ISO8601 string (e.g. 2026-03-05T18:09:41.830+07:00).
         * If null/blank, use now().
         */
        String transactionDate
) {}
