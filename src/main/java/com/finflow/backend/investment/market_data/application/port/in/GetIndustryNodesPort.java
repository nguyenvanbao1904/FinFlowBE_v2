package com.finflow.backend.investment.market_data.application.port.in;
import com.finflow.backend.investment.market_data.application.query.GetIndustryNodesQuery;

import com.finflow.backend.investment.market_data.application.dto.IndustryNodeReadOutput;

import java.util.List;

public interface GetIndustryNodesPort {

    List<IndustryNodeReadOutput> execute(GetIndustryNodesQuery query);
}
