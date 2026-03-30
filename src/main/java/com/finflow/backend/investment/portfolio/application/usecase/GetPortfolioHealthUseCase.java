package com.finflow.backend.investment.portfolio.application.usecase;

import com.finflow.backend.investment.portfolio.application.service.PortfolioHealthComputationService;
import com.finflow.backend.investment.portfolio.presentation.response.PortfolioHealthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetPortfolioHealthUseCase {

    private final PortfolioHealthComputationService portfolioHealthComputationService;

    @Transactional(readOnly = true)
    public PortfolioHealthResponse execute(String userId, UUID portfolioId, int quartersLimit) {
        return portfolioHealthComputationService.compute(userId, portfolioId, quartersLimit);
    }
}
