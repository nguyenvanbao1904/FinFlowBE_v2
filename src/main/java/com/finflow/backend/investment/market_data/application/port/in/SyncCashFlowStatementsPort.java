package com.finflow.backend.investment.market_data.application.port.in;

import com.finflow.backend.investment.market_data.application.command.SyncCashFlowStatementsCommand;

public interface SyncCashFlowStatementsPort {

    void execute(SyncCashFlowStatementsCommand command);
}
