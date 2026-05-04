package com.finflow.backend.investment.portfolio.application.query;

import java.util.UUID;

public record GetTradeTransactionsQuery(
    String userId,
    UUID portfolioId,
    int page,
    int size
) {}
