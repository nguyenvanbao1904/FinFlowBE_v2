package com.finflow.backend.investment.portfolio.application.port.in;

import com.finflow.backend.investment.portfolio.presentation.response.PortfolioAssetResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.finflow.backend.investment.portfolio.domain.entity.PortfolioAsset;
import java.math.RoundingMode;

public interface GetPortfolioAssetsPort {
    List<PortfolioAssetResponse> execute(String userId, java.util.UUID portfolioId);
}
