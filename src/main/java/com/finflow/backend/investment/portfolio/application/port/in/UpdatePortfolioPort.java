package com.finflow.backend.investment.portfolio.application.port.in;

import com.finflow.backend.investment.portfolio.application.command.UpdatePortfolioCommand;
import com.finflow.backend.investment.portfolio.application.dto.PortfolioResponseOutput;

public interface UpdatePortfolioPort {
    PortfolioResponseOutput execute(UpdatePortfolioCommand command);
}
