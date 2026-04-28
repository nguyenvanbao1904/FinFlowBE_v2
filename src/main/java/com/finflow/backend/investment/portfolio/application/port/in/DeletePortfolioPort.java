package com.finflow.backend.investment.portfolio.application.port.in;

import com.finflow.backend.investment.portfolio.application.command.DeletePortfolioCommand;

public interface DeletePortfolioPort {
    void execute(DeletePortfolioCommand command);
}
