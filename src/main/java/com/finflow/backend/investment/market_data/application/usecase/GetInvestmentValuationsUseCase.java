package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.mapper.InvestmentAnalysisPointMapper;
import com.finflow.backend.investment.market_data.application.port.in.GetInvestmentValuationsPort;
import com.finflow.backend.investment.market_data.application.query.GetInvestmentValuationsQuery;
import com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisLimits;
import com.finflow.backend.investment.market_data.application.service.MarketDataReadService;
import com.finflow.backend.investment.market_data.domain.entity.Company;
import com.finflow.backend.investment.market_data.domain.entity.FinancialIndicator;
import com.finflow.backend.investment.market_data.application.dto.InvestmentAnalysisOutput;
import com.finflow.backend.investment.market_data.application.dto.InvestmentValuationPointsOutput;
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
    public InvestmentValuationPointsOutput execute(GetInvestmentValuationsQuery request) {
        if (request.startDate() == null || request.endDate() == null) {
            return new InvestmentValuationPointsOutput(executeWithoutDateRange(request.symbol(), request.annualLimit()));
        }
        LocalDate start = readService.parseIsoDate(request.startDate(), "startDate");
        LocalDate end = readService.parseIsoDate(request.endDate(), "endDate");

        boolean sq = Optional.ofNullable(request.showQuarterly()).orElse(false);
        if (start.isAfter(end)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }

        Company company = readService.resolveCompany(request.symbol());
        List<FinancialIndicator> indicators =
                readService.loadFinancialIndicatorsForValuationsByRange(company.getId(), start, end, sq);

        List<InvestmentAnalysisOutput.ValuationPoint> valuations = indicators.stream()
                .map(pointMapper::toValuationPoint)
                .sorted(VALUATION_ASC)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        return new InvestmentValuationPointsOutput(
                InvestmentAnalysisLimits.applyValuationYearLimit(valuations, request.annualLimit(), VALUATION_ASC));
    }

    private List<InvestmentAnalysisOutput.ValuationPoint> executeWithoutDateRange(String rawSymbol, Integer annualLimit) {
        Company company = readService.resolveCompany(rawSymbol);
        List<FinancialIndicator> indicators = readService.loadFinancialIndicatorsForValuations(company.getId(), annualLimit);
        List<InvestmentAnalysisOutput.ValuationPoint> valuations = indicators.stream()
                .map(pointMapper::toValuationPoint)
                .sorted(VALUATION_ASC)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        return InvestmentAnalysisLimits.applyValuationYearLimit(valuations, annualLimit, VALUATION_ASC);
    }
}
