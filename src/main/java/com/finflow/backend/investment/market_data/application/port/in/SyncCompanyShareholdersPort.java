package com.finflow.backend.investment.market_data.application.port.in;
import com.finflow.backend.investment.market_data.application.command.SyncCompanyShareholdersCommand;

public interface SyncCompanyShareholdersPort {

    void execute(SyncCompanyShareholdersCommand command);
}
