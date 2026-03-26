package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetInvestmentDividendsUseCase {

    private final InvestmentAnalysisService service;

    @Transactional(readOnly = true)
    public List<InvestmentAnalysisResponse.DividendPoint> execute(
            String symbol,
            Integer annualLimit
    ) {
        return service.executeDividends(symbol, annualLimit);
    }
}

