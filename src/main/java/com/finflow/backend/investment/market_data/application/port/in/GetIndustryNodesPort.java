package com.finflow.backend.investment.market_data.application.port.in;

import com.finflow.backend.investment.market_data.presentation.response.IndustryNodeReadResponse;

import java.util.List;

public interface GetIndustryNodesPort {

    List<IndustryNodeReadResponse> execute();
}
