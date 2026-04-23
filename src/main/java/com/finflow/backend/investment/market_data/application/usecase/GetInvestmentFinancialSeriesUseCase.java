package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.port.in.GetInvestmentFinancialSeriesPort;
import com.finflow.backend.investment.market_data.application.query.GetInvestmentFinancialSeriesQuery;
import com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisFinancialSeriesLoader;
import com.finflow.backend.investment.market_data.application.service.MarketDataReadService;
import com.finflow.backend.investment.market_data.domain.entity.Company;
import com.finflow.backend.investment.market_data.domain.entity.FinancialIndicator;
import com.finflow.backend.investment.market_data.application.dto.InvestmentAnalysisOutput;
import com.finflow.backend.investment.market_data.application.dto.InvestmentFinancialSeriesOutput;
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
    public InvestmentFinancialSeriesOutput execute(GetInvestmentFinancialSeriesQuery request) {
        Company company = readService.resolveCompany(request.symbol());
        List<FinancialIndicator> indicators = readService.loadFinancialIndicators(company.getId(), request.annualLimit(), request.quarterlyLimit());
        InvestmentAnalysisOutput.FinancialSeries series = financialSeriesLoader.build(company.getId(), company.getCompanyType(), indicators, request.annualLimit(), request.quarterlyLimit());
        return new InvestmentFinancialSeriesOutput(series);
    }
}
