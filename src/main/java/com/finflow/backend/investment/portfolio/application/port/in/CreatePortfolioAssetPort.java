package com.finflow.backend.investment.portfolio.application.port.in;

import com.finflow.backend.investment.portfolio.application.command.CreatePortfolioAssetCommand;

import com.finflow.backend.common.application.dto.UuidOutput;

public interface CreatePortfolioAssetPort {
    UuidOutput execute(CreatePortfolioAssetCommand command);
}
