package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisService;
import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class GetInvestmentFinancialSeriesUseCase {

    private final InvestmentAnalysisService service;

    @Transactional(readOnly = true)
    public InvestmentAnalysisResponse.FinancialSeries execute(
            String symbol,
            Integer annualLimit,
            Integer quarterlyLimit
    ) {
        return service.executeFinancialSeries(symbol, annualLimit, quarterlyLimit);
    }
}
