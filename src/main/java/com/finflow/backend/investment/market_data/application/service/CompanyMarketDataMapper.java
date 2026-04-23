package com.finflow.backend.investment.market_data.application.service;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.investment.market_data.application.dto.CompanyMarketDataOutput;
import com.finflow.backend.investment.market_data.domain.entity.*;
import com.finflow.backend.investment.market_data.exception.MarketDataErrorCode;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

@Component
public class CompanyMarketDataMapper {

    public EnumSet<Section> resolveSections(List<String> rawSections) {
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

    public CompanyMarketDataOutput.CompanyData toCompanyData(Company c) {
        IndustryNode node = c.getIndustryNode();
        return new CompanyMarketDataOutput.CompanyData(
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

    public CompanyMarketDataOutput.ShareholderData toShareholderData(CompanyShareholder s) {
        return new CompanyMarketDataOutput.ShareholderData(
                s.getId(),
                s.getCompanyId(),
                s.getShareholderName(),
                s.getQuantity(),
                s.getShareOwnPercent(),
                s.getUpdateDate()
        );
    }

    public CompanyMarketDataOutput.DividendData toDividendData(CompanyDividend d) {
        return new CompanyMarketDataOutput.DividendData(
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

    public CompanyMarketDataOutput.FinancialIndicatorData toFinancialIndicatorData(FinancialIndicator f) {
        return new CompanyMarketDataOutput.FinancialIndicatorData(
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

    public CompanyMarketDataOutput.BankBalanceSheetData toBankBalanceSheetData(BankBalanceSheet b) {
        return new CompanyMarketDataOutput.BankBalanceSheetData(
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

    public CompanyMarketDataOutput.NonBankBalanceSheetData toNonBankBalanceSheetData(NonBankBalanceSheet b) {
        return new CompanyMarketDataOutput.NonBankBalanceSheetData(
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

    public CompanyMarketDataOutput.BankIncomeStatementData toBankIncomeStatementData(BankIncomeStatement i) {
        return new CompanyMarketDataOutput.BankIncomeStatementData(
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

    public CompanyMarketDataOutput.NonBankIncomeStatementData toNonBankIncomeStatementData(NonBankIncomeStatement i) {
        return new CompanyMarketDataOutput.NonBankIncomeStatementData(
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

    public enum Section {
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

        public String apiName() {
            return apiName;
        }

        public static Section fromApiName(String name) {
            for (Section s : values()) {
                if (s.apiName.equals(name)) {
                    return s;
                }
            }
            throw new AppException(MarketDataErrorCode.INVALID_READ_SECTION);
        }
    }
}
