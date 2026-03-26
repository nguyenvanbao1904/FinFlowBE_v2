package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
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

