package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.domain.entity.Company;
import com.finflow.backend.investment.market_data.domain.entity.FinancialIndicator;
import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.finflow.backend.investment.market_data.application.usecase.InvestmentAnalysisNumberUtils.toDouble;

@Component
class InvestmentAnalysisOverviewBuilder {

    InvestmentAnalysisResponse.Overview build(Company company, List<FinancialIndicator> indicators) {
        FinancialIndicator latest = selectOverviewIndicator(indicators);
        String industryLabel = company.getIndustryNode() == null || company.getIndustryNode().getNameVi() == null
                ? ""
                : company.getIndustryNode().getNameVi();
        String icbCode = company.getIndustryNode() == null ? null : company.getIndustryNode().getIcbCode();

        return new InvestmentAnalysisResponse.Overview(
                company.getId(),
                company.getCompanyName(),
                company.getExchange(),
                company.getCompanyType(),
                icbCode,
                industryLabel,
                company.getDescription(),
                latest == null ? null : toDouble(latest.getRoe()),
                latest == null ? null : toDouble(latest.getRoa()),
                InvestmentFinancialUtils.computeEpsTtm(indicators),
                latest == null ? null : toDouble(latest.getBvps()),
                latest == null ? null : toDouble(latest.getCplh()),
                latest == null ? null : toDouble(latest.getPe()),
                InvestmentFinancialUtils.median(indicators.stream().map(FinancialIndicator::getPe).toList()),
                latest == null ? null : toDouble(latest.getPb()),
                InvestmentFinancialUtils.median(indicators.stream().map(FinancialIndicator::getPb).toList()),
                latest == null ? null : toDouble(latest.getPs()),
                InvestmentFinancialUtils.median(indicators.stream().map(FinancialIndicator::getPs).toList())
        );
    }

    private static FinancialIndicator selectOverviewIndicator(List<FinancialIndicator> indicators) {
        if (indicators == null || indicators.isEmpty()) return null;
        // Prefer annual snapshot (Q4) for overview metrics like EPS/BVPS/CPLH.
        Optional<FinancialIndicator> latestQ4 = indicators.stream()
                .filter(i -> i.getQuarter() == 4)
                .max(Comparator.comparingInt(FinancialIndicator::getYear));
        if (latestQ4.isPresent()) return latestQ4.get();
        // Fallback to latest available quarter.
        return indicators.stream()
                .max(
                        Comparator.comparingInt(FinancialIndicator::getYear)
                                .thenComparingInt(FinancialIndicator::getQuarter)
                )
                .orElse(null);
    }
}

