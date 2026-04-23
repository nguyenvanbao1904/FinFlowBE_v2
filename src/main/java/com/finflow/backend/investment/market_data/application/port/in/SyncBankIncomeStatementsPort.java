package com.finflow.backend.investment.market_data.application.port.in;
import com.finflow.backend.investment.market_data.application.command.SyncBankIncomeStatementsCommand;

public interface SyncBankIncomeStatementsPort {

    void execute(SyncBankIncomeStatementsCommand command);
}
