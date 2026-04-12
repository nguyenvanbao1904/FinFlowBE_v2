package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.mapper.InvestmentAnalysisPointMapper;
import com.finflow.backend.investment.market_data.application.port.in.GetInvestmentDividendsPort;
import com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisLimits;
import com.finflow.backend.investment.market_data.application.service.MarketDataReadService;
import com.finflow.backend.investment.market_data.domain.entity.Company;
import com.finflow.backend.investment.market_data.domain.entity.CompanyDividend;
import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GetInvestmentDividendsUseCase implements GetInvestmentDividendsPort {

    private final MarketDataReadService readService;
    private final InvestmentAnalysisPointMapper pointMapper;

    @Transactional(readOnly = true)
    @Override
    public List<InvestmentAnalysisResponse.DividendPoint> execute(
            String symbol,
            Integer annualLimit
    ) {
        Company company = readService.resolveCompany(symbol);
        List<CompanyDividend> dividends = readService.loadCompanyDividends(company.getId(), annualLimit);
        List<InvestmentAnalysisResponse.DividendPoint> dividendPoints = dividends.stream()
                .map(pointMapper::toDividendPoint)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        return InvestmentAnalysisLimits.applyDividendYearLimit(dividendPoints, annualLimit);
    }
}
