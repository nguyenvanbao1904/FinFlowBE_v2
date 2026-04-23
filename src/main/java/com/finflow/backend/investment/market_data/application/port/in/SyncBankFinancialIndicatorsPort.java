package com.finflow.backend.investment.market_data.application.port.in;
import com.finflow.backend.investment.market_data.application.command.SyncBankFinancialIndicatorsCommand;

public interface SyncBankFinancialIndicatorsPort {

    void execute(SyncBankFinancialIndicatorsCommand command);
}
