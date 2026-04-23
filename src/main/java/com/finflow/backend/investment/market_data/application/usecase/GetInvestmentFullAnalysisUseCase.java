package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.mapper.InvestmentAnalysisPointMapper;
import com.finflow.backend.investment.market_data.application.port.in.GetInvestmentFullAnalysisPort;
import com.finflow.backend.investment.market_data.application.query.GetInvestmentFullAnalysisQuery;
import com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisFinancialSeriesLoader;
import com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisLimits;
import com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisOverviewBuilder;
import com.finflow.backend.investment.market_data.application.service.MarketDataReadService;
import com.finflow.backend.investment.market_data.domain.entity.Company;
import com.finflow.backend.investment.market_data.domain.entity.CompanyDividend;
import com.finflow.backend.investment.market_data.domain.entity.CompanyShareholder;
import com.finflow.backend.investment.market_data.domain.entity.FinancialIndicator;
import com.finflow.backend.investment.market_data.application.dto.InvestmentAnalysisOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisValuationOrdering.VALUATION_ASC;

@Component
@RequiredArgsConstructor
public class GetInvestmentFullAnalysisUseCase implements GetInvestmentFullAnalysisPort {

    private final MarketDataReadService readService;
    private final InvestmentAnalysisOverviewBuilder overviewBuilder;
    private final InvestmentAnalysisFinancialSeriesLoader financialSeriesLoader;
    private final InvestmentAnalysisPointMapper pointMapper;

    @Transactional(readOnly = true)
    @Override
    public InvestmentAnalysisOutput execute(GetInvestmentFullAnalysisQuery request) {
        Company company = readService.resolveCompany(request.symbol());

        List<FinancialIndicator> indicators = readService.loadFinancialIndicators(company.getId(), request.annualLimit(), request.quarterlyLimit());
        List<CompanyShareholder> shareholders = readService.loadShareholders(company.getId());
        List<CompanyDividend> dividends = readService.loadCompanyDividends(company.getId(), request.annualLimit());

        InvestmentAnalysisOutput.Overview overview = overviewBuilder.build(company, indicators);
        List<InvestmentAnalysisOutput.ValuationPoint> valuations = indicators.stream()
                .map(pointMapper::toValuationPoint)
                .sorted(VALUATION_ASC)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        valuations = InvestmentAnalysisLimits.applyValuationYearLimit(valuations, request.annualLimit(), VALUATION_ASC);

        List<InvestmentAnalysisOutput.DividendPoint> dividendPoints = dividends.stream()
                .map(pointMapper::toDividendPoint)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        dividendPoints = InvestmentAnalysisLimits.applyDividendYearLimit(dividendPoints, request.annualLimit());

        List<InvestmentAnalysisOutput.ShareholderPoint> shareholderPoints = shareholders.stream()
                .map(pointMapper::toShareholderPoint)
                .toList();

        InvestmentAnalysisOutput.FinancialSeries financials =
                financialSeriesLoader.build(company.getId(), company.getCompanyType(), indicators, request.annualLimit(), request.quarterlyLimit());
        return new InvestmentAnalysisOutput(overview, shareholderPoints, valuations, financials, dividendPoints);
    }
}
