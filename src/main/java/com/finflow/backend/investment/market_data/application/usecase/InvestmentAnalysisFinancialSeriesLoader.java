package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.domain.entity.*;
import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
class InvestmentAnalysisFinancialSeriesLoader {
    private final InvestmentAnalysisRepositoryLoader repositoryLoader;
    private final InvestmentFinancialSeriesBuilder financialSeriesBuilder;

    InvestmentAnalysisResponse.FinancialSeries build(
            String companyId,
            String companyType,
            List<FinancialIndicator> indicators,
            Integer annualLimit,
            Integer quarterlyLimit
    ) {
        String normalizedType = Optional.ofNullable(companyType).orElse("").toUpperCase();
        if ("BANK".equals(normalizedType)) {
            List<BankBalanceSheet> balances = repositoryLoader.loadBankBalances(companyId, annualLimit, quarterlyLimit);
            List<BankIncomeStatement> incomes = repositoryLoader.loadBankIncomes(companyId, annualLimit, quarterlyLimit);
            return financialSeriesBuilder.build(
                    companyType,
                    indicators,
                    annualLimit,
                    quarterlyLimit,
                    balances,
                    incomes,
                    null,
                    null
            );
        }

        List<NonBankBalanceSheet> balances = repositoryLoader.loadNonBankBalances(companyId, annualLimit, quarterlyLimit);
        List<NonBankIncomeStatement> incomes = repositoryLoader.loadNonBankIncomes(companyId, annualLimit, quarterlyLimit);
        return financialSeriesBuilder.build(
                companyType,
                indicators,
                annualLimit,
                quarterlyLimit,
                null,
                null,
                balances,
                incomes
        );
    }
}

