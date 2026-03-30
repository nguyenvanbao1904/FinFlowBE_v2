package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.investment.market_data.domain.entity.*;
import com.finflow.backend.investment.market_data.domain.repository.*;
import com.finflow.backend.investment.market_data.exception.MarketDataErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

/**
 * Loads domain entities required for investment analysis.
 * <p>
 * No @Transactional here by design; transaction boundary stays in UseCase layer.
 */
@Component
@RequiredArgsConstructor
class InvestmentAnalysisRepositoryLoader {
    private final CompanyRepository companyRepository;
    private final CompanyShareholderRepository companyShareholderRepository;
    private final CompanyDividendRepository companyDividendRepository;
    private final FinancialIndicatorRepository financialIndicatorRepository;
    private final BankBalanceSheetRepository bankBalanceSheetRepository;
    private final NonBankBalanceSheetRepository nonBankBalanceSheetRepository;
    private final BankIncomeStatementRepository bankIncomeStatementRepository;
    private final NonBankIncomeStatementRepository nonBankIncomeStatementRepository;

    Company resolveCompany(String rawSymbol) {
        String symbol = Optional.ofNullable(rawSymbol).orElse("").trim().toUpperCase();
        if (symbol.isEmpty()) {
            throw new AppException(MarketDataErrorCode.INVALID_SYMBOL);
        }
        return companyRepository.findByIdIgnoreCase(symbol)
                .orElseThrow(() -> new AppException(MarketDataErrorCode.COMPANY_NOT_FOUND));
    }

    List<CompanyShareholder> loadShareholders(String companyId) {
        return companyShareholderRepository.findByCompanyIdOrderByShareOwnPercentDesc(companyId);
    }

    List<CompanyDividend> loadAllCompanyDividendsOrderByRecordDateAsc(String companyId) {
        return companyDividendRepository.findByCompanyIdOrderByRecordDateAsc(companyId);
    }

    List<FinancialIndicator> loadAllFinancialIndicatorsAsc(String companyId) {
        return financialIndicatorRepository.findByCompanyIdOrderByYearAscQuarterAsc(companyId);
    }

    List<NonBankIncomeStatement> loadAllNonBankIncomesAsc(String companyId) {
        return nonBankIncomeStatementRepository.findByCompanyIdOrderByYearAscQuarterAsc(companyId);
    }

    List<BankIncomeStatement> loadAllBankIncomesAsc(String companyId) {
        return bankIncomeStatementRepository.findByCompanyIdOrderByYearAscQuarterAsc(companyId);
    }

    /** {@code limit} quý gần nhất (year/quarter DESC), dùng cho TTM “doanh thu” NH. */
    List<BankIncomeStatement> loadBankIncomesLastQuarters(String companyId, int limit) {
        int cap = Math.max(1, limit);
        return new ArrayList<>(
                bankIncomeStatementRepository.findByCompanyIdOrderByYearDescQuarterDesc(companyId, PageRequest.of(0, cap))
        );
    }

    List<FinancialIndicator> loadFinancialIndicators(String companyId, Integer annualLimit, Integer quarterlyLimit) {
        if (!isBoundedLoad(annualLimit, quarterlyLimit)) {
            return financialIndicatorRepository.findByCompanyIdOrderByYearAscQuarterAsc(companyId);
        }
        int cap = computeIndicatorCap(annualLimit, quarterlyLimit);
        List<FinancialIndicator> rows = new ArrayList<>(
                financialIndicatorRepository.findByCompanyIdOrderByYearDescQuarterDesc(companyId, PageRequest.of(0, cap))
        );
        rows.sort(Comparator.comparing(FinancialIndicator::getYear).thenComparing(FinancialIndicator::getQuarter));
        return rows;
    }

    List<FinancialIndicator> loadFinancialIndicatorsForValuations(String companyId, Integer annualLimit) {
        if (annualLimit == null) {
            return financialIndicatorRepository.findByCompanyIdOrderByYearAscQuarterAsc(companyId);
        }
        int cap = (int) Math.min(800L, Math.max(24L, (long) annualLimit * 4 + 16L));
        List<FinancialIndicator> rows = new ArrayList<>(
                financialIndicatorRepository.findByCompanyIdOrderByYearDescQuarterDesc(companyId, PageRequest.of(0, cap))
        );
        rows.sort(Comparator.comparing(FinancialIndicator::getYear).thenComparing(FinancialIndicator::getQuarter));
        return rows;
    }

    List<FinancialIndicator> loadFinancialIndicatorsForValuationsByRange(
            String companyId,
            LocalDate startDate,
            LocalDate endDate,
            boolean showQuarterly
    ) {
        if (!showQuarterly) {
            int startYear = startDate.getYear();
            int endYear = endDate.getYear();
            LocalDate endDec31 = LocalDate.of(endYear, 12, 31);
            int endYearIncluded = endDate.isBefore(endDec31) ? endYear - 1 : endYear;
            if (endYearIncluded < startYear) {
                return List.of();
            }
            return financialIndicatorRepository.findByCompanyIdAndYearBetweenOrderByYearAscQuarterAsc(
                    companyId,
                    startYear,
                    endYearIncluded
            );
        }

        int startYear = startDate.getYear();
        int endYear = endDate.getYear();
        int startQuarterMin = quarterMinForDate(startDate);
        int endQuarterMax = quarterMaxForDate(endDate);

        if (startQuarterMin == 0 || endQuarterMax == 0 || startQuarterMin > 4 || endQuarterMax > 4) {
            return List.of();
        }

        if (startYear == endYear) {
            if (startQuarterMin > endQuarterMax) {
                return List.of();
            }
            return financialIndicatorRepository.findByCompanyIdAndYearAndQuarterBetweenOrderByYearAscQuarterAsc(
                    companyId,
                    startYear,
                    startQuarterMin,
                    endQuarterMax
            );
        }

        List<FinancialIndicator> combined = new ArrayList<>();

        // Start year segment: (startQuarterMin..4)
        if (startQuarterMin <= 4) {
            combined.addAll(
                    financialIndicatorRepository.findByCompanyIdAndYearAndQuarterBetweenOrderByYearAscQuarterAsc(
                            companyId,
                            startYear,
                            startQuarterMin,
                            4
                    )
            );
        }

        // Mid years: (startYear+1..endYear-1)
        if (startYear + 1 <= endYear - 1) {
            combined.addAll(
                    financialIndicatorRepository.findByCompanyIdAndYearBetweenOrderByYearAscQuarterAsc(
                            companyId,
                            startYear + 1,
                            endYear - 1
                    )
            );
        }

        // End year segment: (1..endQuarterMax)
        if (endQuarterMax >= 1) {
            combined.addAll(
                    financialIndicatorRepository.findByCompanyIdAndYearAndQuarterBetweenOrderByYearAscQuarterAsc(
                            companyId,
                            endYear,
                            1,
                            endQuarterMax
                    )
            );
        }

        return combined;
    }

    List<CompanyDividend> loadCompanyDividends(String companyId, Integer annualLimit) {
        if (annualLimit == null) {
            return companyDividendRepository.findByCompanyIdOrderByRecordDateAsc(companyId);
        }
        int cap = (int) Math.min(5000L, Math.max(48L, (long) annualLimit * 60L));
        List<CompanyDividend> rows = new ArrayList<>(
                companyDividendRepository.findByCompanyIdOrderByRecordDateDesc(companyId, PageRequest.of(0, cap))
        );
        rows.sort(Comparator.comparing(CompanyDividend::getRecordDate, Comparator.nullsLast(Comparator.naturalOrder())));
        return rows;
    }

    List<BankBalanceSheet> loadBankBalances(String companyId, Integer annualLimit, Integer quarterlyLimit) {
        if (!isBoundedLoad(annualLimit, quarterlyLimit)) {
            return bankBalanceSheetRepository.findByCompanyIdOrderByYearAscQuarterAsc(companyId);
        }
        int cap = computeTimeSeriesCap(annualLimit, quarterlyLimit);
        List<BankBalanceSheet> rows = new ArrayList<>(
                bankBalanceSheetRepository.findByCompanyIdOrderByYearDescQuarterDesc(companyId, PageRequest.of(0, cap))
        );
        rows.sort(Comparator.comparing(BankBalanceSheet::getYear).thenComparing(BankBalanceSheet::getQuarter));
        return rows;
    }

    List<BankIncomeStatement> loadBankIncomes(String companyId, Integer annualLimit, Integer quarterlyLimit) {
        if (!isBoundedLoad(annualLimit, quarterlyLimit)) {
            return bankIncomeStatementRepository.findByCompanyIdOrderByYearAscQuarterAsc(companyId);
        }
        int cap = computeTimeSeriesCap(annualLimit, quarterlyLimit);
        List<BankIncomeStatement> rows = new ArrayList<>(
                bankIncomeStatementRepository.findByCompanyIdOrderByYearDescQuarterDesc(companyId, PageRequest.of(0, cap))
        );
        rows.sort(Comparator.comparing(BankIncomeStatement::getYear).thenComparing(BankIncomeStatement::getQuarter));
        return rows;
    }

    List<NonBankBalanceSheet> loadNonBankBalances(String companyId, Integer annualLimit, Integer quarterlyLimit) {
        if (!isBoundedLoad(annualLimit, quarterlyLimit)) {
            return nonBankBalanceSheetRepository.findByCompanyIdOrderByYearAscQuarterAsc(companyId);
        }
        int cap = computeTimeSeriesCap(annualLimit, quarterlyLimit);
        List<NonBankBalanceSheet> rows = new ArrayList<>(
                nonBankBalanceSheetRepository.findByCompanyIdOrderByYearDescQuarterDesc(companyId, PageRequest.of(0, cap))
        );
        rows.sort(Comparator.comparing(NonBankBalanceSheet::getYear).thenComparing(NonBankBalanceSheet::getQuarter));
        return rows;
    }

    List<NonBankIncomeStatement> loadNonBankIncomes(String companyId, Integer annualLimit, Integer quarterlyLimit) {
        if (!isBoundedLoad(annualLimit, quarterlyLimit)) {
            return nonBankIncomeStatementRepository.findByCompanyIdOrderByYearAscQuarterAsc(companyId);
        }
        int cap = computeTimeSeriesCap(annualLimit, quarterlyLimit);
        List<NonBankIncomeStatement> rows = new ArrayList<>(
                nonBankIncomeStatementRepository.findByCompanyIdOrderByYearDescQuarterDesc(companyId, PageRequest.of(0, cap))
        );
        rows.sort(Comparator.comparing(NonBankIncomeStatement::getYear).thenComparing(NonBankIncomeStatement::getQuarter));
        return rows;
    }

    /** {@code limit} quý gần nhất (year/quarter DESC), dùng cho DTT TTM. */
    List<NonBankIncomeStatement> loadNonBankIncomesLastQuarters(String companyId, int limit) {
        int cap = Math.max(1, limit);
        return new ArrayList<>(
                nonBankIncomeStatementRepository.findByCompanyIdOrderByYearDescQuarterDesc(companyId, PageRequest.of(0, cap))
        );
    }

    LocalDate parseIsoDate(String raw, String paramName) {
        try {
            return LocalDate.parse(raw);
        } catch (Exception e) {
            throw new AppException(MarketDataErrorCode.INVALID_ISO_DATE);
        }
    }

    private static int quarterMinForDate(LocalDate date) {
        int year = date.getYear();
        LocalDate q1 = LocalDate.of(year, 3, 31);
        LocalDate q2 = LocalDate.of(year, 6, 30);
        LocalDate q3 = LocalDate.of(year, 9, 30);
        // First quarter whose quarter-end >= startDate.
        if (!date.isAfter(q1)) return 1;
        if (!date.isAfter(q2)) return 2;
        if (!date.isAfter(q3)) return 3;
        return 4;
    }

    private static int quarterMaxForDate(LocalDate date) {
        int year = date.getYear();
        LocalDate q1 = LocalDate.of(year, 3, 31);
        LocalDate q2 = LocalDate.of(year, 6, 30);
        LocalDate q3 = LocalDate.of(year, 9, 30);
        LocalDate q4 = LocalDate.of(year, 12, 31);
        // Last quarter whose quarter-end <= endDate.
        if (date.isBefore(q1)) return 0;
        if (date.isBefore(q2)) return 1;
        if (date.isBefore(q3)) return 2;
        if (date.isBefore(q4)) return 3;
        return 4;
    }

    private static boolean isBoundedLoad(Integer annualLimit, Integer quarterlyLimit) {
        return annualLimit != null || quarterlyLimit != null;
    }

    private static int computeTimeSeriesCap(Integer annualLimit, Integer quarterlyLimit) {
        int al = annualLimit != null ? Math.max(annualLimit, 0) : 4;
        int ql = quarterlyLimit != null ? Math.max(quarterlyLimit, 0) : 4;
        long cap = 12L + (long) al * 4 + (long) ql;
        if (annualLimit == null) {
            cap = Math.max(cap, 128L + (long) ql * 2);
        }
        if (quarterlyLimit == null) {
            cap = Math.max(cap, 128L + (long) al * 8);
        }
        return (int) Math.min(5000L, Math.max(32L, cap));
    }

    private static int computeIndicatorCap(Integer annualLimit, Integer quarterlyLimit) {
        return (int) Math.min(800L, Math.max(24L, (long) computeTimeSeriesCap(annualLimit, quarterlyLimit) + 32L));
    }
}

