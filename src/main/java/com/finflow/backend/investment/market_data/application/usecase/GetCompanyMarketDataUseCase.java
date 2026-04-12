package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.investment.market_data.application.port.in.GetCompanyMarketDataPort;
import com.finflow.backend.investment.market_data.application.service.MarketDataReadService;
import com.finflow.backend.investment.market_data.domain.entity.*;
import com.finflow.backend.investment.market_data.exception.MarketDataErrorCode;
import com.finflow.backend.investment.market_data.presentation.response.CompanyMarketDataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class GetCompanyMarketDataUseCase implements GetCompanyMarketDataPort {

    private final MarketDataReadService readService;

    @Transactional(readOnly = true)
    @Override
    public CompanyMarketDataResponse execute(
            String symbol,
            List<String> includes,
            Integer annualLimit,
            Integer quarterlyLimit
    ) {
        Company company = readService.resolveCompany(symbol);
        EnumSet<Section> sections = resolveSections(includes);

        CompanyMarketDataResponse.CompanyData companyData = null;
        List<CompanyMarketDataResponse.ShareholderData> shareholders = null;
        List<CompanyMarketDataResponse.DividendData> dividends = null;
        List<CompanyMarketDataResponse.FinancialIndicatorData> financialIndicators = null;
        List<CompanyMarketDataResponse.BankBalanceSheetData> bankBalanceSheets = null;
        List<CompanyMarketDataResponse.NonBankBalanceSheetData> nonBankBalanceSheets = null;
        List<CompanyMarketDataResponse.BankIncomeStatementData> bankIncomeStatements = null;
        List<CompanyMarketDataResponse.NonBankIncomeStatementData> nonBankIncomeStatements = null;

        if (sections.contains(Section.COMPANY)) {
            companyData = toCompanyData(company);
        }
        if (sections.contains(Section.SHAREHOLDERS)) {
            shareholders = readService.loadShareholders(company.getId()).stream()
                    .map(GetCompanyMarketDataUseCase::toShareholderData)
                    .toList();
        }
        if (sections.contains(Section.DIVIDENDS)) {
            dividends = readService.loadCompanyDividends(company.getId(), annualLimit).stream()
                    .map(GetCompanyMarketDataUseCase::toDividendData)
                    .toList();
        }
        if (sections.contains(Section.FINANCIAL_INDICATORS)) {
            financialIndicators = readService.loadFinancialIndicators(company.getId(), annualLimit, quarterlyLimit).stream()
                    .map(GetCompanyMarketDataUseCase::toFinancialIndicatorData)
                    .toList();
        }
        if (sections.contains(Section.BANK_BALANCE_SHEETS)) {
            bankBalanceSheets = "BANK".equalsIgnoreCase(company.getCompanyType())
                    ? readService.loadBankBalances(company.getId(), annualLimit, quarterlyLimit).stream()
                    .map(GetCompanyMarketDataUseCase::toBankBalanceSheetData)
                    .toList()
                    : List.of();
        }
        if (sections.contains(Section.NON_BANK_BALANCE_SHEETS)) {
            nonBankBalanceSheets = "BANK".equalsIgnoreCase(company.getCompanyType())
                    ? List.of()
                    : readService.loadNonBankBalances(company.getId(), annualLimit, quarterlyLimit).stream()
                    .map(GetCompanyMarketDataUseCase::toNonBankBalanceSheetData)
                    .toList();
        }
        if (sections.contains(Section.BANK_INCOME_STATEMENTS)) {
            bankIncomeStatements = "BANK".equalsIgnoreCase(company.getCompanyType())
                    ? readService.loadBankIncomes(company.getId(), annualLimit, quarterlyLimit).stream()
                    .map(GetCompanyMarketDataUseCase::toBankIncomeStatementData)
                    .toList()
                    : List.of();
        }
        if (sections.contains(Section.NON_BANK_INCOME_STATEMENTS)) {
            nonBankIncomeStatements = "BANK".equalsIgnoreCase(company.getCompanyType())
                    ? List.of()
                    : readService.loadNonBankIncomes(company.getId(), annualLimit, quarterlyLimit).stream()
                    .map(GetCompanyMarketDataUseCase::toNonBankIncomeStatementData)
                    .toList();
        }

        return new CompanyMarketDataResponse(
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

    private static EnumSet<Section> resolveSections(List<String> rawSections) {
        if (rawSections == null || rawSections.isEmpty()) {
            return EnumSet.of(Section.COMPANY);
        }
        EnumSet<Section> out = EnumSet.noneOf(Section.class);
        for (String raw : rawSections) {
            String token = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
            if (token.isEmpty()) {
                continue;
            }
            if ("all".equals(token)) {
                return EnumSet.allOf(Section.class);
            }
            out.add(Section.fromApiName(token));
        }
        if (out.isEmpty()) {
            return EnumSet.of(Section.COMPANY);
        }
        return out;
    }

    private static CompanyMarketDataResponse.CompanyData toCompanyData(Company c) {
        IndustryNode node = c.getIndustryNode();
        return new CompanyMarketDataResponse.CompanyData(
                c.getId(),
                c.getExchange(),
                c.getCompanyName(),
                c.getDescription(),
                c.getCompanyType(),
                node == null ? null : node.getId(),
                node == null || node.getParent() == null ? null : node.getParent().getId(),
                node == null ? null : node.getLevel(),
                node == null ? null : node.getIcbCode(),
                node == null ? null : node.getNameVi(),
                node == null ? null : node.getDetailLabel()
        );
    }

    private static CompanyMarketDataResponse.ShareholderData toShareholderData(CompanyShareholder s) {
        return new CompanyMarketDataResponse.ShareholderData(
                s.getId(),
                s.getCompanyId(),
                s.getShareholderName(),
                s.getQuantity(),
                s.getShareOwnPercent(),
                s.getUpdateDate()
        );
    }

    private static CompanyMarketDataResponse.DividendData toDividendData(CompanyDividend d) {
        return new CompanyMarketDataResponse.DividendData(
                d.getId(),
                d.getCompanyId(),
                d.getEventTitle(),
                d.getEventType(),
                d.getRatio(),
                d.getValue(),
                d.getRecordDate(),
                d.getExrightDate(),
                d.getIssueDate()
        );
    }

    private static CompanyMarketDataResponse.FinancialIndicatorData toFinancialIndicatorData(FinancialIndicator f) {
        return new CompanyMarketDataResponse.FinancialIndicatorData(
                f.getId(),
                f.getCompanyId(),
                f.getYear(),
                f.getQuarter(),
                f.getPe(),
                f.getPb(),
                f.getPs(),
                f.getRoe(),
                f.getRoa(),
                f.getEps(),
                f.getBvps(),
                f.getLng(),
                f.getLnr(),
                f.getCplh()
        );
    }

    private static CompanyMarketDataResponse.BankBalanceSheetData toBankBalanceSheetData(BankBalanceSheet b) {
        return new CompanyMarketDataResponse.BankBalanceSheetData(
                b.getId(),
                b.getCompanyId(),
                b.getYear(),
                b.getQuarter(),
                b.getCashAndCashEquivalents(),
                b.getTotalAssets(),
                b.getEquity(),
                b.getTotalCapital(),
                b.getBalancesWithSbv(),
                b.getInterbankPlacementsAndLoans(),
                b.getTradingSecurities(),
                b.getInvestmentSecurities(),
                b.getLoansToCustomers(),
                b.getGovAndSbvDebt(),
                b.getDepositsBorrowingsOthers(),
                b.getDepositsFromCustomers(),
                b.getConvertibleAndOtherPapers(),
                b.getTotalLiabilities()
        );
    }

    private static CompanyMarketDataResponse.NonBankBalanceSheetData toNonBankBalanceSheetData(NonBankBalanceSheet b) {
        return new CompanyMarketDataResponse.NonBankBalanceSheetData(
                b.getId(),
                b.getCompanyId(),
                b.getYear(),
                b.getQuarter(),
                b.getCashAndCashEquivalents(),
                b.getTotalAssets(),
                b.getEquity(),
                b.getTotalCapital(),
                b.getShortTermInvestments(),
                b.getShortTermReceivables(),
                b.getLongTermReceivables(),
                b.getInventories(),
                b.getFixedAssets(),
                b.getShortTermBorrowings(),
                b.getLongTermBorrowings(),
                b.getAdvancesFromCustomers(),
                b.getTotalLiabilities()
        );
    }

    private static CompanyMarketDataResponse.BankIncomeStatementData toBankIncomeStatementData(BankIncomeStatement i) {
        return new CompanyMarketDataResponse.BankIncomeStatementData(
                i.getId(),
                i.getCompanyId(),
                i.getYear(),
                i.getQuarter(),
                i.getProfitAfterTax(),
                i.getInterestExpense(),
                i.getNetInterestIncome(),
                i.getNetFeeAndCommissionIncome(),
                i.getNetOtherIncomeOrExpenses(),
                i.getNetProfit()
        );
    }

    private static CompanyMarketDataResponse.NonBankIncomeStatementData toNonBankIncomeStatementData(NonBankIncomeStatement i) {
        return new CompanyMarketDataResponse.NonBankIncomeStatementData(
                i.getId(),
                i.getCompanyId(),
                i.getYear(),
                i.getQuarter(),
                i.getProfitAfterTax(),
                i.getNetRevenue(),
                i.getTotalRevenue(),
                i.getNetProfit()
        );
    }

    private enum Section {
        COMPANY("company"),
        SHAREHOLDERS("shareholders"),
        DIVIDENDS("dividends"),
        FINANCIAL_INDICATORS("financialIndicators"),
        BANK_BALANCE_SHEETS("bankBalanceSheets"),
        NON_BANK_BALANCE_SHEETS("nonBankBalanceSheets"),
        BANK_INCOME_STATEMENTS("bankIncomeStatements"),
        NON_BANK_INCOME_STATEMENTS("nonBankIncomeStatements");

        private final String apiName;

        Section(String apiName) {
            this.apiName = apiName;
        }

        String apiName() {
            return apiName;
        }

        static Section fromApiName(String name) {
            for (Section s : values()) {
                if (s.apiName.equals(name)) {
                    return s;
                }
            }
            throw new AppException(MarketDataErrorCode.INVALID_READ_SECTION);
        }
    }
}
