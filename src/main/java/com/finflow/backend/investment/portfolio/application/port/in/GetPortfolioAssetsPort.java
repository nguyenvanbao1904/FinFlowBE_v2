package com.finflow.backend.investment.portfolio.application.port.in;
import com.finflow.backend.investment.portfolio.application.query.GetPortfolioAssetsQuery;

import com.finflow.backend.investment.portfolio.application.dto.PortfolioAssetOutput;
import java.util.List;

public interface GetPortfolioAssetsPort {
    List<PortfolioAssetOutput> execute(GetPortfolioAssetsQuery query);
}
