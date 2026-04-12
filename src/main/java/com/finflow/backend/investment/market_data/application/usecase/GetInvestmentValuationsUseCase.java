package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.mapper.InvestmentAnalysisPointMapper;
import com.finflow.backend.investment.market_data.application.port.in.GetInvestmentValuationsPort;
import com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisLimits;
import com.finflow.backend.investment.market_data.application.service.MarketDataReadService;
import com.finflow.backend.investment.market_data.domain.entity.Company;
import com.finflow.backend.investment.market_data.domain.entity.FinancialIndicator;
import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisValuationOrdering.VALUATION_ASC;

@Component
@RequiredArgsConstructor
public class GetInvestmentValuationsUseCase implements GetInvestmentValuationsPort {

    private final MarketDataReadService readService;
    private final InvestmentAnalysisPointMapper pointMapper;

    @Transactional(readOnly = true)
    @Override
    public List<InvestmentAnalysisResponse.ValuationPoint> execute(
            String symbol,
            Integer annualLimit,
            String startDate,
            String endDate,
            Boolean showQuarterly
    ) {
        if (startDate == null || endDate == null) {
            return executeWithoutDateRange(symbol, annualLimit);
        }
        LocalDate start = readService.parseIsoDate(startDate, "startDate");
        LocalDate end = readService.parseIsoDate(endDate, "endDate");

        boolean sq = Optional.ofNullable(showQuarterly).orElse(false);
        if (start.isAfter(end)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }

        Company company = readService.resolveCompany(symbol);
        List<FinancialIndicator> indicators =
                readService.loadFinancialIndicatorsForValuationsByRange(company.getId(), start, end, sq);

        List<InvestmentAnalysisResponse.ValuationPoint> valuations = indicators.stream()
                .map(pointMapper::toValuationPoint)
                .sorted(VALUATION_ASC)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        return InvestmentAnalysisLimits.applyValuationYearLimit(valuations, annualLimit, VALUATION_ASC);
    }

    private List<InvestmentAnalysisResponse.ValuationPoint> executeWithoutDateRange(String rawSymbol, Integer annualLimit) {
        Company company = readService.resolveCompany(rawSymbol);
        List<FinancialIndicator> indicators = readService.loadFinancialIndicatorsForValuations(company.getId(), annualLimit);
        List<InvestmentAnalysisResponse.ValuationPoint> valuations = indicators.stream()
                .map(pointMapper::toValuationPoint)
                .sorted(VALUATION_ASC)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        return InvestmentAnalysisLimits.applyValuationYearLimit(valuations, annualLimit, VALUATION_ASC);
    }
}
