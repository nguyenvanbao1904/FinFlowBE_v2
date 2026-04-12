package com.finflow.backend.investment.portfolio.application.port.in;

import com.finflow.backend.investment.portfolio.presentation.response.PortfolioAssetResponse;
import java.math.BigDecimal;
import com.finflow.backend.investment.portfolio.application.command.CreatePortfolioAssetCommand;
import com.finflow.backend.investment.portfolio.domain.entity.PortfolioAsset;
import java.math.RoundingMode;
import java.util.UUID;
import com.finflow.backend.investment.portfolio.domain.entity.Portfolio;

public interface CreatePortfolioAssetPort {
    PortfolioAssetResponse execute(CreatePortfolioAssetCommand command);
}
