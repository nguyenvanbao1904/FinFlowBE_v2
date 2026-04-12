package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.port.in.GetInvestmentFinancialSeriesPort;
import com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisFinancialSeriesLoader;
import com.finflow.backend.investment.market_data.application.service.MarketDataReadService;
import com.finflow.backend.investment.market_data.domain.entity.Company;
import com.finflow.backend.investment.market_data.domain.entity.FinancialIndicator;
import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetInvestmentFinancialSeriesUseCase implements GetInvestmentFinancialSeriesPort {

    private final MarketDataReadService readService;
    private final InvestmentAnalysisFinancialSeriesLoader financialSeriesLoader;

    @Transactional(readOnly = true)
    @Override
    public InvestmentAnalysisResponse.FinancialSeries execute(
            String symbol,
            Integer annualLimit,
            Integer quarterlyLimit
    ) {
        Company company = readService.resolveCompany(symbol);
        List<FinancialIndicator> indicators = readService.loadFinancialIndicators(company.getId(), annualLimit, quarterlyLimit);
        return financialSeriesLoader.build(company.getId(), company.getCompanyType(), indicators, annualLimit, quarterlyLimit);
    }
}
