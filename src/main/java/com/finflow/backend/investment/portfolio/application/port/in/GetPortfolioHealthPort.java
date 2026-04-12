package com.finflow.backend.investment.portfolio.application.port.in;
import com.finflow.backend.investment.portfolio.application.result.PortfolioHealthResult;
import java.util.UUID;

public interface GetPortfolioHealthPort {
    PortfolioHealthResult execute(String userId, UUID portfolioId, int quartersLimit);
}
