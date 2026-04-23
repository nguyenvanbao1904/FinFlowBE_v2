package com.finflow.backend.investment.market_data.application.port.in;

import com.finflow.backend.investment.market_data.application.dto.InvestmentDividendPointsOutput;
import com.finflow.backend.investment.market_data.application.query.GetInvestmentDividendsQuery;

public interface GetInvestmentDividendsPort {

    InvestmentDividendPointsOutput execute(GetInvestmentDividendsQuery query);
}
