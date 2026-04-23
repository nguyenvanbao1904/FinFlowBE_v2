package com.finflow.backend.investment.market_data.application.port.in;
import com.finflow.backend.investment.market_data.application.command.SyncCompanyDividendsCommand;

public interface SyncCompanyDividendsPort {

    void execute(SyncCompanyDividendsCommand command);
}
