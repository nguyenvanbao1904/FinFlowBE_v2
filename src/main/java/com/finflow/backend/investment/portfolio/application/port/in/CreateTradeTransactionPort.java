package com.finflow.backend.investment.portfolio.application.port.in;

import com.finflow.backend.investment.portfolio.application.command.CreateTradeTransactionCommand;

public interface CreateTradeTransactionPort {
    void execute(CreateTradeTransactionCommand command);
}
