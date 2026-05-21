package com.finflow.backend.investment.portfolio.application.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record PortfolioResponseOutput(
        UUID id,
        String name,
        UUID wealthAccountId,
        BigDecimal cashBalance,
        BigDecimal totalCostBasis,
        BigDecimal totalMarketValueClose,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
