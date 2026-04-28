package com.finflow.backend.investment.portfolio.application.port.in;

import com.finflow.backend.investment.portfolio.application.command.CreatePortfolioCommand;
import com.finflow.backend.investment.portfolio.application.dto.PortfolioResponseOutput;

public interface CreatePortfolioPort {
    PortfolioResponseOutput execute(CreatePortfolioCommand command);
}
