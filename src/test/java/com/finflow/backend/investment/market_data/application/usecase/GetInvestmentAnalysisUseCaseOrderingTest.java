package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.mapper.InvestmentAnalysisPointMapper;
import com.finflow.backend.investment.market_data.application.mapper.InvestmentFinancialPointMapper;
import com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisFinancialSeriesLoader;
import com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisOverviewBuilder;
import com.finflow.backend.investment.market_data.application.service.InvestmentFinancialSeriesBuilder;
import com.finflow.backend.investment.market_data.application.service.MarketDataReadService;
import com.finflow.backend.investment.market_data.domain.entity.BankBalanceSheet;
import com.finflow.backend.investment.market_data.domain.entity.BankFinancialIndicator;
import com.finflow.backend.investment.market_data.domain.entity.BankIncomeStatement;
import com.finflow.backend.investment.market_data.domain.entity.Company;
import com.finflow.backend.investment.market_data.domain.repository.*;
import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;
import com.finflow.backend.investment.portfolio.infrastructure.VpsMarketPriceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetInvestmentAnalysisUseCaseOrderingTest {

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
    VpsMarketPriceClient vpsMarketPriceClient;

    private GetInvestmentFullAnalysisUseCase useCase;

    @BeforeEach
    void setUp() {
        InvestmentAnalysisPointMapper pointMapper = Mappers.getMapper(InvestmentAnalysisPointMapper.class);
        InvestmentFinancialPointMapper financialPointMapper =
                Mappers.getMapper(InvestmentFinancialPointMapper.class);

        InvestmentFinancialSeriesBuilder financialSeriesBuilder = new InvestmentFinancialSeriesBuilder(financialPointMapper);
        MarketDataReadService readService = new MarketDataReadService(
                companyRepository,
                industryNodeRepository,
                companyShareholderRepository,
                companyDividendRepository,
                financialIndicatorRepository,
                bankBalanceSheetRepository,
                nonBankBalanceSheetRepository,
                bankIncomeStatementRepository,
                nonBankIncomeStatementRepository
        );
        InvestmentAnalysisOverviewBuilder overviewBuilder =
                new InvestmentAnalysisOverviewBuilder(vpsMarketPriceClient, readService);
        InvestmentAnalysisFinancialSeriesLoader financialSeriesLoader =
                new InvestmentAnalysisFinancialSeriesLoader(readService, financialSeriesBuilder);

        useCase = new GetInvestmentFullAnalysisUseCase(
                readService,
                overviewBuilder,
                financialSeriesLoader,
                pointMapper
        );
    }

    @Test
    void valuations_and_bank_financials_are_chronological_when_limited() {
        Company c = new Company();
        c.setId("ACB");
        c.setCompanyType("BANK");
        c.setExchange("HOSE");
        c.setCompanyName("Test Bank");
        when(companyRepository.findByIdIgnoreCase("ACB")).thenReturn(Optional.of(c));

        BankFinancialIndicator i2024 = indicator(2024, 4);
        BankFinancialIndicator i2023 = indicator(2023, 4);
        when(financialIndicatorRepository.findByCompanyIdOrderByYearDescQuarterDesc(eq("ACB"), any(Pageable.class)))
                .thenReturn(List.of(i2024, i2023));

        when(companyShareholderRepository.findByCompanyIdOrderByShareOwnPercentDesc("ACB")).thenReturn(List.of());
        when(companyDividendRepository.findByCompanyIdOrderByRecordDateDesc(eq("ACB"), any(Pageable.class))).thenReturn(List.of());

        BankBalanceSheet bb = new BankBalanceSheet();
        bb.setCompanyId("ACB");
        bb.setYear(2024);
        bb.setQuarter(4);
        bb.setEquity(BigDecimal.TEN);
        BankIncomeStatement inc = new BankIncomeStatement();
        inc.setCompanyId("ACB");
        inc.setYear(2024);
        inc.setQuarter(1);
        inc.setProfitAfterTax(BigDecimal.ONE);
        when(bankBalanceSheetRepository.findByCompanyIdOrderByYearDescQuarterDesc(eq("ACB"), any(Pageable.class)))
                .thenReturn(List.of(bb));
        when(bankIncomeStatementRepository.findByCompanyIdOrderByYearDescQuarterDesc(eq("ACB"), any(Pageable.class)))
                .thenReturn(List.of(inc));

        InvestmentAnalysisResponse r = useCase.execute("ACB", 4, 4);

        Comparator<InvestmentAnalysisResponse.ValuationPoint> valuationAsc = Comparator
                .comparing(InvestmentAnalysisResponse.ValuationPoint::year, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(InvestmentAnalysisResponse.ValuationPoint::quarter, Comparator.nullsLast(Comparator.naturalOrder()));
        assertThat(r.valuations()).isSortedAccordingTo(valuationAsc);

        List<InvestmentAnalysisResponse.BankFinancialPoint> bank = r.financials().bank();
        assertThat(bank).isNotEmpty();
        Comparator<InvestmentAnalysisResponse.BankFinancialPoint> bankAsc = Comparator
                .comparing(InvestmentAnalysisResponse.BankFinancialPoint::year, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(InvestmentAnalysisResponse.BankFinancialPoint::quarter, Comparator.nullsLast(Comparator.naturalOrder()));
        assertThat(bank).isSortedAccordingTo(bankAsc);
    }

    private static BankFinancialIndicator indicator(int year, int quarter) {
        BankFinancialIndicator i = new BankFinancialIndicator();
        i.setCompanyId("ACB");
        i.setYear(year);
        i.setQuarter(quarter);
        i.setPe(BigDecimal.valueOf(5));
        i.setPb(BigDecimal.ONE);
        i.setPs(BigDecimal.ONE);
        return i;
    }
}
