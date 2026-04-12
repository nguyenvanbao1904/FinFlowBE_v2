package com.finflow.backend.investment.market_data.application.port.in;

import com.finflow.backend.investment.market_data.presentation.request.IndustryNodeRequestDTO;

import java.util.List;

public interface SyncIndustryNodesPort {

    void execute(List<IndustryNodeRequestDTO> requests);
}
