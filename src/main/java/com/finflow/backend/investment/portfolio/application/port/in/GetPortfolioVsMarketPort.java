package com.finflow.backend.investment.portfolio.application.port.in;

import java.util.List;
import com.finflow.backend.investment.portfolio.presentation.response.PortfolioMarketBenchmarkResponse;
import java.util.Map;
import java.util.Comparator;
import java.util.UUID;

public interface GetPortfolioVsMarketPort {
    PortfolioMarketBenchmarkResponse execute(String userId, UUID portfolioId, String benchmarkCode);
}
