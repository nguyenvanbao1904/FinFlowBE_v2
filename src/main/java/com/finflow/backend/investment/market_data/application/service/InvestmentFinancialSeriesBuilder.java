package com.finflow.backend.investment.market_data.application.service;

import com.finflow.backend.investment.market_data.domain.entity.*;
import com.finflow.backend.investment.market_data.application.mapper.InvestmentFinancialPointMapper;
import com.finflow.backend.investment.market_data.application.strategy.BankStatementStrategy;
import com.finflow.backend.investment.market_data.application.strategy.NonBankStatementStrategy;
import com.finflow.backend.investment.market_data.application.dto.InvestmentAnalysisOutput;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

/**
 * Director for building InvestmentAnalysisOutput.FinancialSeries.
 * Uses Strategy pattern to avoid BANK/NON_BANK code branching in the director itself.
 */
@Component
public class InvestmentFinancialSeriesBuilder {
    private final BankStatementStrategy bankStrategy;
    private final NonBankStatementStrategy nonBankStrategy;

    public InvestmentFinancialSeriesBuilder(InvestmentFinancialPointMapper pointMapper) {
        Assert.notNull(pointMapper, "pointMapper must not be null");
        this.bankStrategy = new BankStatementStrategy(pointMapper);
        this.nonBankStrategy = new NonBankStatementStrategy(pointMapper);
    }

    public InvestmentAnalysisOutput.FinancialSeries build(
            String companyType,
            List<FinancialIndicator> indicators,
            Integer annualLimit,
            Integer quarterlyLimit,
            List<BankBalanceSheet> bankBalances,
            List<BankIncomeStatement> bankIncomes,
            List<NonBankBalanceSheet> nonBankBalances,
            List<NonBankIncomeStatement> nonBankIncomes
    ) {
        String normalizedType = Optional.ofNullable(companyType).orElse("").toUpperCase();
        if ("BANK".equals(normalizedType)) {
            List<InvestmentAnalysisOutput.BankFinancialPoint> points =
                    bankStrategy.buildPoints(
                            bankBalances,
                            bankIncomes,
                            indicators,
                            annualLimit,
                            quarterlyLimit
                    );
            return new InvestmentAnalysisOutput.FinancialSeries("BANK", points, List.of());
        }

        List<InvestmentAnalysisOutput.NonBankFinancialPoint> points =
                nonBankStrategy.buildPoints(
                        nonBankBalances,
                        nonBankIncomes,
                        indicators,
                        annualLimit,
                        quarterlyLimit
                );
        return new InvestmentAnalysisOutput.FinancialSeries("NON_BANK", List.of(), points);
    }
}
