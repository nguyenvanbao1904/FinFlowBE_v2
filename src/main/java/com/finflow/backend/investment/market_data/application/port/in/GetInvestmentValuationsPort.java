package com.finflow.backend.investment.market_data.application.port.in;

import com.finflow.backend.investment.market_data.application.dto.InvestmentValuationPointsOutput;
import com.finflow.backend.investment.market_data.application.query.GetInvestmentValuationsQuery;

public interface GetInvestmentValuationsPort {

    InvestmentValuationPointsOutput execute(GetInvestmentValuationsQuery query);
}
