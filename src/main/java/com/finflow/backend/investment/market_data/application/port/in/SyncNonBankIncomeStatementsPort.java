package com.finflow.backend.investment.market_data.application.port.in;
import com.finflow.backend.investment.market_data.application.command.SyncNonBankIncomeStatementsCommand;

public interface SyncNonBankIncomeStatementsPort {

    void execute(SyncNonBankIncomeStatementsCommand command);
}
