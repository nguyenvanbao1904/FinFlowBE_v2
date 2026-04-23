package com.finflow.backend.investment.market_data.application.port.in;
import com.finflow.backend.investment.market_data.application.command.SyncIndustryNodesCommand;

public interface SyncIndustryNodesPort {

    void execute(SyncIndustryNodesCommand command);
}
