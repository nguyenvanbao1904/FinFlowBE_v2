package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
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

