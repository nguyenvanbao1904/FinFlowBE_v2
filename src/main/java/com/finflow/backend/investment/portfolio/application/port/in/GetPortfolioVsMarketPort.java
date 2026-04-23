package com.finflow.backend.investment.portfolio.application.port.in;

import com.finflow.backend.investment.portfolio.application.dto.PortfolioMarketBenchmarkOutput;
import com.finflow.backend.investment.portfolio.application.query.GetPortfolioVsMarketQuery;

public interface GetPortfolioVsMarketPort {
    PortfolioMarketBenchmarkOutput execute(GetPortfolioVsMarketQuery query);
}
