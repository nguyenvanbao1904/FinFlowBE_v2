package com.finflow.backend.investment.portfolio.domain.repository;

import java.math.BigDecimal;
import java.util.UUID;

public interface PortfolioStockCostBasisProjection {
    UUID getPortfolioId();

    BigDecimal getStockCostBasis();
}
