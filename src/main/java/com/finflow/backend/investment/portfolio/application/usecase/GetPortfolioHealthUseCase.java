package com.finflow.backend.investment.portfolio.application.usecase;

import com.finflow.backend.investment.portfolio.application.port.in.GetPortfolioHealthPort;

import com.finflow.backend.investment.portfolio.application.result.PortfolioHealthResult;
import com.finflow.backend.investment.portfolio.application.service.PortfolioHealthComputationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetPortfolioHealthUseCase implements GetPortfolioHealthPort {

    private final PortfolioHealthComputationService portfolioHealthComputationService;

    @Transactional(readOnly = true)
    @Override
    public PortfolioHealthResult execute(String userId, UUID portfolioId, int quartersLimit) {
        return portfolioHealthComputationService.compute(userId, portfolioId, quartersLimit);
    }
}
