package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.domain.entity.BankFinancialIndicator;
import com.finflow.backend.investment.market_data.domain.entity.Company;
import com.finflow.backend.investment.market_data.domain.entity.FinancialIndicator;
import com.finflow.backend.investment.market_data.domain.repository.*;
import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;
import com.finflow.backend.investment.portfolio.infrastructure.VpsMarketPriceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetInvestmentAnalysisUseCaseValuationRangeTest {

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
    VpsMarketPriceClient vpsMarketPriceClient;

    private InvestmentAnalysisService service;

    @BeforeEach
    void setUp() {
        InvestmentAnalysisPointMapper pointMapper = Mappers.getMapper(InvestmentAnalysisPointMapper.class);
        InvestmentFinancialPointMapper financialPointMapper =
                Mappers.getMapper(InvestmentFinancialPointMapper.class);

        InvestmentFinancialSeriesBuilder financialSeriesBuilder = new InvestmentFinancialSeriesBuilder(financialPointMapper);
        InvestmentAnalysisRepositoryLoader repositoryLoader = new InvestmentAnalysisRepositoryLoader(
                companyRepository,
                companyShareholderRepository,
                companyDividendRepository,
                financialIndicatorRepository,
                bankBalanceSheetRepository,
                nonBankBalanceSheetRepository,
                bankIncomeStatementRepository,
                nonBankIncomeStatementRepository
        );
        InvestmentAnalysisOverviewBuilder overviewBuilder =
                new InvestmentAnalysisOverviewBuilder(vpsMarketPriceClient, repositoryLoader);
        InvestmentAnalysisFinancialSeriesLoader financialSeriesLoader =
                new InvestmentAnalysisFinancialSeriesLoader(repositoryLoader, financialSeriesBuilder);

        service = new InvestmentAnalysisService(
                repositoryLoader,
                overviewBuilder,
                financialSeriesLoader,
                pointMapper
        );
    }

    @Test
    void annualMode_shouldExcludeEndYearIfEndDateBeforeDec31() {
        Company c = company();
        when(companyRepository.findByIdIgnoreCase("ACB")).thenReturn(Optional.of(c));

        // startYear=2020, endYear=2020 nhưng endDate trước 12/31/2020 => endYearIncluded=2019 => rỗng
        List<InvestmentAnalysisResponse.ValuationPoint> result =
                service.executeValuations("ACB", null, "2020-01-01", "2020-11-30", false);

        assertThat(result).isEmpty();
        verify(financialIndicatorRepository, never())
                .findByCompanyIdAndYearBetweenOrderByYearAscQuarterAsc(eq("ACB"), anyInt(), anyInt());
    }

    @Test
    void quarterlyMode_sameYear_shouldQueryQuarterBetween() {
        Company c = company();
        when(companyRepository.findByIdIgnoreCase("ACB")).thenReturn(Optional.of(c));

        FinancialIndicator q1 = indicator(2020, 1);
        FinancialIndicator q2 = indicator(2020, 2);
        when(financialIndicatorRepository.findByCompanyIdAndYearAndQuarterBetweenOrderByYearAscQuarterAsc(
                        eq("ACB"),
                        eq(2020),
                        eq(1),
                        eq(2)
                ))
                .thenReturn(List.of(q1, q2));

        List<InvestmentAnalysisResponse.ValuationPoint> result =
                service.executeValuations("ACB", null, "2020-02-01", "2020-08-15", true);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).year()).isEqualTo(2020);
        assertThat(result.get(0).quarter()).isEqualTo(1);
        assertThat(result.get(1).year()).isEqualTo(2020);
        assertThat(result.get(1).quarter()).isEqualTo(2);

        verify(financialIndicatorRepository, never())
                .findByCompanyIdAndYearBetweenOrderByYearAscQuarterAsc(eq("ACB"), anyInt(), anyInt());
    }

    @Test
    void quarterlyMode_crossYear_shouldCombineStartAndEndSegments() {
        Company c = company();
        when(companyRepository.findByIdIgnoreCase("ACB")).thenReturn(Optional.of(c));

        FinancialIndicator end2020q4 = indicator(2020, 4);
        FinancialIndicator start2021q1 = indicator(2021, 1);

        when(financialIndicatorRepository.findByCompanyIdAndYearAndQuarterBetweenOrderByYearAscQuarterAsc(
                        eq("ACB"),
                        eq(2020),
                        eq(4),
                        eq(4)
                ))
                .thenReturn(List.of(end2020q4));

        when(financialIndicatorRepository.findByCompanyIdAndYearAndQuarterBetweenOrderByYearAscQuarterAsc(
                        eq("ACB"),
                        eq(2021),
                        eq(1),
                        eq(1)
                ))
                .thenReturn(List.of(start2021q1));

        List<InvestmentAnalysisResponse.ValuationPoint> result =
                service.executeValuations("ACB", null, "2020-11-15", "2021-03-31", true);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).year()).isEqualTo(2020);
        assertThat(result.get(0).quarter()).isEqualTo(4);
        assertThat(result.get(1).year()).isEqualTo(2021);
        assertThat(result.get(1).quarter()).isEqualTo(1);

        verify(financialIndicatorRepository, never())
                .findByCompanyIdAndYearBetweenOrderByYearAscQuarterAsc(eq("ACB"), eq(2021), eq(2020));
    }

    private static Company company() {
        Company c = new Company();
        c.setId("ACB");
        return c;
    }

    private static BankFinancialIndicator indicator(int year, int quarter) {
        BankFinancialIndicator i = new BankFinancialIndicator();
        i.setCompanyId("ACB");
        i.setYear(year);
        i.setQuarter(quarter);
        i.setPe(BigDecimal.valueOf(5));
        i.setPb(BigDecimal.ONE);
        i.setPs(BigDecimal.TEN);
        return i;
    }
}

