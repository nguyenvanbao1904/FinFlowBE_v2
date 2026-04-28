package com.finflow.backend.investment.portfolio.application.port.in;

import com.finflow.backend.investment.portfolio.application.command.CreatePortfolioAssetCommand;
import com.finflow.backend.investment.portfolio.application.dto.PortfolioAssetOutput;

public interface CreatePortfolioAssetPort {
    PortfolioAssetOutput execute(CreatePortfolioAssetCommand command);
}
