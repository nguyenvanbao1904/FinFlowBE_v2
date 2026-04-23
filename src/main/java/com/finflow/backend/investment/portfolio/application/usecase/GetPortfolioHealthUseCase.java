package com.finflow.backend.investment.portfolio.application.usecase;

import com.finflow.backend.investment.portfolio.application.port.in.GetPortfolioHealthPort;
import com.finflow.backend.investment.portfolio.application.query.GetPortfolioHealthQuery;

import com.finflow.backend.investment.portfolio.application.dto.PortfolioHealthOutput;
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
    public PortfolioHealthOutput execute(GetPortfolioHealthQuery request) {
        String userId = request.userId();
        UUID portfolioId = request.portfolioId();
        int quartersLimit = request.quartersLimit();
        return portfolioHealthComputationService.compute(userId, portfolioId, quartersLimit);
    }
}
