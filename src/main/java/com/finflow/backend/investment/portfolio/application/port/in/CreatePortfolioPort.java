package com.finflow.backend.investment.portfolio.application.port.in;

import com.finflow.backend.investment.portfolio.application.command.CreatePortfolioCommand;

import com.finflow.backend.common.application.dto.UuidOutput;

public interface CreatePortfolioPort {
    UuidOutput execute(CreatePortfolioCommand command);
}
