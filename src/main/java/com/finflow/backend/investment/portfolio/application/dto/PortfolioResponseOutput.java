package com.finflow.backend.investment.portfolio.application.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record PortfolioResponseOutput(
        UUID id,
        String name,
        BigDecimal cashBalance,
        BigDecimal totalCostBasis,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
