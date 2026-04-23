package com.finflow.backend.investment.portfolio.application.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record PortfolioAssetOutput(
        String symbol,
        BigDecimal totalQuantity,
        BigDecimal averagePrice,
        BigDecimal closePrice,
        BigDecimal marketValueClose,
        BigDecimal unrealizedPnL,
        BigDecimal unrealizedPnLPct,
        LocalDateTime updatedAt
) {}
