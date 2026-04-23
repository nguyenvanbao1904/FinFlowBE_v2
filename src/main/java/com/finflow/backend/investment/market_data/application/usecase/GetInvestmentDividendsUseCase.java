package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.mapper.InvestmentAnalysisPointMapper;
import com.finflow.backend.investment.market_data.application.port.in.GetInvestmentDividendsPort;
import com.finflow.backend.investment.market_data.application.query.GetInvestmentDividendsQuery;
import com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisLimits;
import com.finflow.backend.investment.market_data.application.service.MarketDataReadService;
import com.finflow.backend.investment.market_data.domain.entity.Company;
import com.finflow.backend.investment.market_data.domain.entity.CompanyDividend;
import com.finflow.backend.investment.market_data.application.dto.InvestmentAnalysisOutput;
import com.finflow.backend.investment.market_data.application.dto.InvestmentDividendPointsOutput;
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
    public InvestmentDividendPointsOutput execute(GetInvestmentDividendsQuery request) {
        Company company = readService.resolveCompany(request.symbol());
        List<CompanyDividend> dividends = readService.loadCompanyDividends(company.getId(), request.annualLimit());
        List<InvestmentAnalysisOutput.DividendPoint> dividendPoints = dividends.stream()
                .map(pointMapper::toDividendPoint)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        return new InvestmentDividendPointsOutput(
                InvestmentAnalysisLimits.applyDividendYearLimit(dividendPoints, request.annualLimit()));
    }
}
