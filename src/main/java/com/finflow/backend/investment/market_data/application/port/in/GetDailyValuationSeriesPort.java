package com.finflow.backend.investment.market_data.application.port.in;

import com.finflow.backend.investment.market_data.application.dto.InvestmentDailyValuationPointsOutput;
import com.finflow.backend.investment.market_data.application.query.GetDailyValuationSeriesQuery;

public interface GetDailyValuationSeriesPort {

    InvestmentDailyValuationPointsOutput execute(GetDailyValuationSeriesQuery query);
}
