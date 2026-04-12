package com.finflow.backend.investment.portfolio.application.port.in;

import com.finflow.backend.investment.portfolio.application.command.CreatePortfolioCommand;
import com.finflow.backend.investment.portfolio.presentation.response.PortfolioResponse;
import java.math.BigDecimal;
import com.finflow.backend.investment.portfolio.domain.entity.Portfolio;

public interface CreatePortfolioPort {
    PortfolioResponse execute(CreatePortfolioCommand command);
}
