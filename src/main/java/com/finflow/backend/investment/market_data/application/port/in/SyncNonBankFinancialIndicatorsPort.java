package com.finflow.backend.investment.market_data.application.port.in;
import com.finflow.backend.investment.market_data.application.command.SyncNonBankFinancialIndicatorsCommand;

public interface SyncNonBankFinancialIndicatorsPort {

    void execute(SyncNonBankFinancialIndicatorsCommand command);
}
