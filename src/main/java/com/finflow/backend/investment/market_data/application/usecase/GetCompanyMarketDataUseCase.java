package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.port.in.GetCompanyMarketDataPort;
import com.finflow.backend.investment.market_data.application.query.GetCompanyMarketDataQuery;
import com.finflow.backend.investment.market_data.application.service.CompanyMarketDataMapper;
import com.finflow.backend.investment.market_data.application.service.CompanyMarketDataMapper.Section;
import com.finflow.backend.investment.market_data.application.service.MarketDataReadService;
import com.finflow.backend.investment.market_data.domain.entity.Company;
import com.finflow.backend.investment.market_data.application.dto.CompanyMarketDataOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GetCompanyMarketDataUseCase implements GetCompanyMarketDataPort {

    private final MarketDataReadService readService;
    private final CompanyMarketDataMapper mapper;

    @Transactional(readOnly = true)
    @Override
    public CompanyMarketDataOutput execute(GetCompanyMarketDataQuery request) {
        Company company = readService.resolveCompany(request.symbol());
        EnumSet<Section> sections = mapper.resolveSections(request.includes());

        CompanyMarketDataOutput.CompanyData companyData = null;
        List<CompanyMarketDataOutput.ShareholderData> shareholders = null;
        List<CompanyMarketDataOutput.DividendData> dividends = null;
        List<CompanyMarketDataOutput.FinancialIndicatorData> financialIndicators = null;
        List<CompanyMarketDataOutput.BankBalanceSheetData> bankBalanceSheets = null;
        List<CompanyMarketDataOutput.NonBankBalanceSheetData> nonBankBalanceSheets = null;
        List<CompanyMarketDataOutput.BankIncomeStatementData> bankIncomeStatements = null;
        List<CompanyMarketDataOutput.NonBankIncomeStatementData> nonBankIncomeStatements = null;

        if (sections.contains(Section.COMPANY)) {
            companyData = mapper.toCompanyData(company);
        }
        if (sections.contains(Section.SHAREHOLDERS)) {
            shareholders = readService.loadShareholders(company.getId()).stream()
                    .map(mapper::toShareholderData).toList();
        }
        if (sections.contains(Section.DIVIDENDS)) {
            dividends = readService.loadCompanyDividends(company.getId(), request.annualLimit()).stream()
                    .map(mapper::toDividendData).toList();
        }
        if (sections.contains(Section.FINANCIAL_INDICATORS)) {
            financialIndicators = readService.loadFinancialIndicators(company.getId(), request.annualLimit(), request.quarterlyLimit()).stream()
                    .map(mapper::toFinancialIndicatorData).toList();
        }
        if (sections.contains(Section.BANK_BALANCE_SHEETS)) {
            bankBalanceSheets = "BANK".equalsIgnoreCase(company.getCompanyType())
                    ? readService.loadBankBalances(company.getId(), request.annualLimit(), request.quarterlyLimit()).stream()
                    .map(mapper::toBankBalanceSheetData).toList()
                    : List.of();
        }
        if (sections.contains(Section.NON_BANK_BALANCE_SHEETS)) {
            nonBankBalanceSheets = "BANK".equalsIgnoreCase(company.getCompanyType())
                    ? List.of()
                    : readService.loadNonBankBalances(company.getId(), request.annualLimit(), request.quarterlyLimit()).stream()
                    .map(mapper::toNonBankBalanceSheetData).toList();
        }
        if (sections.contains(Section.BANK_INCOME_STATEMENTS)) {
            bankIncomeStatements = "BANK".equalsIgnoreCase(company.getCompanyType())
                    ? readService.loadBankIncomes(company.getId(), request.annualLimit(), request.quarterlyLimit()).stream()
                    .map(mapper::toBankIncomeStatementData).toList()
                    : List.of();
        }
        if (sections.contains(Section.NON_BANK_INCOME_STATEMENTS)) {
            nonBankIncomeStatements = "BANK".equalsIgnoreCase(company.getCompanyType())
                    ? List.of()
                    : readService.loadNonBankIncomes(company.getId(), request.annualLimit(), request.quarterlyLimit()).stream()
                    .map(mapper::toNonBankIncomeStatementData).toList();
        }

        return new CompanyMarketDataOutput(
                company.getId(),
                company.getCompanyType(),
                sections.stream().map(Section::apiName).toList(),
                companyData,
                shareholders,
                dividends,
                financialIndicators,
                bankBalanceSheets,
                nonBankBalanceSheets,
                bankIncomeStatements,
                nonBankIncomeStatements
        );
    }
}
