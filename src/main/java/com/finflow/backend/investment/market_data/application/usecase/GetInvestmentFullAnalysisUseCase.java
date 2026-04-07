package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisService;
import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class GetInvestmentFullAnalysisUseCase {

    private final InvestmentAnalysisService service;

    @Transactional(readOnly = true)
    public InvestmentAnalysisResponse execute(
            String symbol,
            Integer annualLimit,
            Integer quarterlyLimit
    ) {
        return service.execute(symbol, annualLimit, quarterlyLimit);
    }
}
