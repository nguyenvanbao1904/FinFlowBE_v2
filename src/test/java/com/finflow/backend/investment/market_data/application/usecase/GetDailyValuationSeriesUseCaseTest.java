package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.dto.InvestmentAnalysisOutput;
import com.finflow.backend.investment.market_data.application.model.StockDailyClose;
import com.finflow.backend.investment.market_data.application.port.out.FetchHistoricalPricePort;
import com.finflow.backend.investment.market_data.application.query.GetDailyValuationSeriesQuery;
import com.finflow.backend.investment.market_data.application.service.MarketDataReadService;
import com.finflow.backend.investment.market_data.domain.entity.Company;
import com.finflow.backend.investment.market_data.domain.entity.NonBankFinancialIndicator;
import com.finflow.backend.investment.market_data.domain.entity.NonBankIncomeStatement;
import com.finflow.backend.investment.market_data.domain.repository.BankBalanceSheetRepository;
import com.finflow.backend.investment.market_data.domain.repository.BankIncomeStatementRepository;
import com.finflow.backend.investment.market_data.domain.repository.CompanyDividendRepository;
import com.finflow.backend.investment.market_data.domain.repository.CompanyRepository;
import com.finflow.backend.investment.market_data.domain.repository.CompanyShareholderRepository;
import com.finflow.backend.investment.market_data.domain.repository.FinancialIndicatorRepository;
import com.finflow.backend.investment.market_data.domain.repository.IndustryNodeRepository;
import com.finflow.backend.investment.market_data.domain.repository.NonBankBalanceSheetRepository;
import com.finflow.backend.investment.market_data.domain.repository.NonBankIncomeStatementRepository;
import com.finflow.backend.investment.market_data.domain.repository.CashFlowStatementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetDailyValuationSeriesUseCaseTest {

    @Mock
    CompanyRepository companyRepository;
    @Mock
    CompanyShareholderRepository companyShareholderRepository;
    @Mock
    CompanyDividendRepository companyDividendRepository;
    @Mock
    FinancialIndicatorRepository financialIndicatorRepository;
    @Mock
    BankBalanceSheetRepository bankBalanceSheetRepository;
    @Mock
    NonBankBalanceSheetRepository nonBankBalanceSheetRepository;
    @Mock
    BankIncomeStatementRepository bankIncomeStatementRepository;
    @Mock
    NonBankIncomeStatementRepository nonBankIncomeStatementRepository;
    @Mock
    IndustryNodeRepository industryNodeRepository;
    @Mock
    CashFlowStatementRepository cashFlowStatementRepository;
    @Mock
    FetchHistoricalPricePort fetchHistoricalPricePort;

    private GetDailyValuationSeriesUseCase useCase;

    @BeforeEach
    void setUp() {
        MarketDataReadService readService = new MarketDataReadService(
                companyRepository,
                industryNodeRepository,
                companyShareholderRepository,
                companyDividendRepository,
                financialIndicatorRepository,
                bankBalanceSheetRepository,
                nonBankBalanceSheetRepository,
                bankIncomeStatementRepository,
                nonBankIncomeStatementRepository,
                cashFlowStatementRepository
        );
        useCase = new GetDailyValuationSeriesUseCase(readService, fetchHistoricalPricePort);
    }

    @Test
    void shouldNotFabricateMissingRangeEndPointFromLatestPrice() {
        Company company = new Company();
        company.setId("AAA");
        company.setCompanyType("NORMAL");
        when(companyRepository.findByIdIgnoreCase("AAA")).thenReturn(Optional.of(company));

        when(financialIndicatorRepository.findByCompanyIdOrderByYearAscQuarterAsc("AAA"))
                .thenReturn(List.of(indicator(2023, 4)));
        when(nonBankIncomeStatementRepository.findByCompanyIdOrderByYearAscQuarterAsc("AAA"))
                .thenReturn(List.of(income(2023, 4)));
        when(fetchHistoricalPricePort.listStockClosesInRange(
                "AAA",
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 2)
        )).thenReturn(List.of(
                new StockDailyClose(LocalDate.of(2024, 1, 1), BigDecimal.valueOf(25_000))
        ));

        List<InvestmentAnalysisOutput.DailyValuationPoint> result =
                useCase.execute(new GetDailyValuationSeriesQuery("AAA", "2024-01-01", "2024-01-02")).points();

        assertThat(result)
                .extracting(InvestmentAnalysisOutput.DailyValuationPoint::date)
                .containsExactly("2024-01-01");
    }

    private static NonBankFinancialIndicator indicator(int year, int quarter) {
        NonBankFinancialIndicator indicator = new NonBankFinancialIndicator();
        indicator.setCompanyId("AAA");
        indicator.setYear(year);
        indicator.setQuarter(quarter);
        indicator.setEps(BigDecimal.valueOf(2_500));
        indicator.setBvps(BigDecimal.valueOf(15_000));
        indicator.setCplh(BigDecimal.valueOf(1_200));
        return indicator;
    }

    private static NonBankIncomeStatement income(int year, int quarter) {
        NonBankIncomeStatement income = new NonBankIncomeStatement();
        income.setCompanyId("AAA");
        income.setYear(year);
        income.setQuarter(quarter);
        income.setNetRevenue(BigDecimal.valueOf(8_000_000_000L));
        return income;
    }
}
