package com.finflow.backend.investment.portfolio.presentation.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PortfolioResponse {
    UUID id;
    String name;
    UUID wealthAccountId;
    BigDecimal cashBalance;
    BigDecimal totalCostBasis;
    BigDecimal totalMarketValueClose;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
