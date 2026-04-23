package com.finflow.backend.investment.portfolio.application.port.in;

import com.finflow.backend.investment.portfolio.application.dto.PortfolioHealthOutput;
import com.finflow.backend.investment.portfolio.application.query.GetPortfolioHealthQuery;

public interface GetPortfolioHealthPort {
    PortfolioHealthOutput execute(GetPortfolioHealthQuery query);
}
