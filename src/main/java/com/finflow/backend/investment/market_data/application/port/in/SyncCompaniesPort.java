package com.finflow.backend.investment.market_data.application.port.in;
import com.finflow.backend.investment.market_data.application.command.SyncCompaniesCommand;

public interface SyncCompaniesPort {

    void execute(SyncCompaniesCommand command);
}
