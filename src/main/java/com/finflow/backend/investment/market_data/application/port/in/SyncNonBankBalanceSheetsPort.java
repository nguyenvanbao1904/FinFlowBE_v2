package com.finflow.backend.investment.market_data.application.port.in;
import com.finflow.backend.investment.market_data.application.command.SyncNonBankBalanceSheetsCommand;

public interface SyncNonBankBalanceSheetsPort {

    void execute(SyncNonBankBalanceSheetsCommand command);
}
