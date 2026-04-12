package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.mapper.InvestmentAnalysisPointMapper;
import com.finflow.backend.investment.market_data.application.port.in.GetInvestmentFullAnalysisPort;
import com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisFinancialSeriesLoader;
import com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisLimits;
import com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisOverviewBuilder;
import com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisValuationOrdering;
import com.finflow.backend.investment.market_data.application.service.MarketDataReadService;
import com.finflow.backend.investment.market_data.domain.entity.Company;
import com.finflow.backend.investment.market_data.domain.entity.CompanyDividend;
import com.finflow.backend.investment.market_data.domain.entity.CompanyShareholder;
import com.finflow.backend.investment.market_data.domain.entity.FinancialIndicator;
import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;
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
    public InvestmentAnalysisResponse execute(
            String symbol,
            Integer annualLimit,
            Integer quarterlyLimit
    ) {
        Company company = readService.resolveCompany(symbol);

        List<FinancialIndicator> indicators = readService.loadFinancialIndicators(company.getId(), annualLimit, quarterlyLimit);
        List<CompanyShareholder> shareholders = readService.loadShareholders(company.getId());
        List<CompanyDividend> dividends = readService.loadCompanyDividends(company.getId(), annualLimit);

        InvestmentAnalysisResponse.Overview overview = overviewBuilder.build(company, indicators);
        List<InvestmentAnalysisResponse.ValuationPoint> valuations = indicators.stream()
                .map(pointMapper::toValuationPoint)
                .sorted(VALUATION_ASC)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        valuations = InvestmentAnalysisLimits.applyValuationYearLimit(valuations, annualLimit, VALUATION_ASC);

        List<InvestmentAnalysisResponse.DividendPoint> dividendPoints = dividends.stream()
                .map(pointMapper::toDividendPoint)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        dividendPoints = InvestmentAnalysisLimits.applyDividendYearLimit(dividendPoints, annualLimit);

        List<InvestmentAnalysisResponse.ShareholderPoint> shareholderPoints = shareholders.stream()
                .map(pointMapper::toShareholderPoint)
                .toList();

        InvestmentAnalysisResponse.FinancialSeries financials =
                financialSeriesLoader.build(company.getId(), company.getCompanyType(), indicators, annualLimit, quarterlyLimit);
        return new InvestmentAnalysisResponse(overview, shareholderPoints, valuations, financials, dividendPoints);
    }
}
