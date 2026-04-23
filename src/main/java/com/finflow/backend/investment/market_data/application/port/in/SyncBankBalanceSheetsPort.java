package com.finflow.backend.investment.market_data.application.port.in;
import com.finflow.backend.investment.market_data.application.command.SyncBankBalanceSheetsCommand;

public interface SyncBankBalanceSheetsPort {

    void execute(SyncBankBalanceSheetsCommand command);
}
