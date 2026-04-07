package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisService;
import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetInvestmentValuationsUseCase {

    private final InvestmentAnalysisService service;

    @Transactional(readOnly = true)
    public List<InvestmentAnalysisResponse.ValuationPoint> execute(
            String symbol,
            Integer annualLimit,
            String startDate,
            String endDate,
            Boolean showQuarterly
    ) {
        return service.executeValuations(symbol, annualLimit, startDate, endDate, showQuarterly);
    }
}
