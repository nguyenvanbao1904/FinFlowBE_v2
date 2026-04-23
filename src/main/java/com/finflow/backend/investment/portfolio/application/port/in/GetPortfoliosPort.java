package com.finflow.backend.investment.portfolio.application.port.in;
import com.finflow.backend.investment.portfolio.application.query.GetPortfoliosQuery;

import com.finflow.backend.investment.portfolio.application.dto.PortfolioResponseOutput;
import java.util.List;

public interface GetPortfoliosPort {
    List<PortfolioResponseOutput> execute(GetPortfoliosQuery query);
}
