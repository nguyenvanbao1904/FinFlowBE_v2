package com.finflow.backend.investment.portfolio.application.port.in;

import com.finflow.backend.investment.portfolio.application.dto.PortfolioInsightItem;
import com.finflow.backend.investment.portfolio.application.query.GetPortfolioInsightsQuery;

import java.util.List;

public interface GetPortfolioInsightsPort {

    List<PortfolioInsightItem> execute(GetPortfolioInsightsQuery query);
}
