package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.domain.entity.Company;
import com.finflow.backend.investment.market_data.domain.entity.CompanyDividend;
import com.finflow.backend.investment.market_data.domain.entity.CompanyShareholder;
import com.finflow.backend.investment.market_data.domain.entity.FinancialIndicator;
import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Phân tích đầu tư: tải BCTC, chỉ số, cổ tức theo mã.
 * <p>
 * <strong>Thứ tự thời gian trong response</strong>: các mảng {@code valuations}, điểm trong {@code financials},
 * và {@code dividends} đều được trả theo thứ tự thời gian <em>tăng dần</em> (cũ → mới) để client vẽ biểu đồ
 * trái-phải không cần sort lại.
 * <p>
 * <strong>Giới hạn</strong>: {@code annualLimit} / {@code quarterlyLimit} — khi cả hai đều {@code null} thì tải
 * đủ lịch sử; khi một trong hai khác {@code null} thì dùng truy vấn có {@code LIMIT} ở DB (xem các {@code load*}).
 */
@Service
@RequiredArgsConstructor
class InvestmentAnalysisService {
    private static final Comparator<InvestmentAnalysisResponse.ValuationPoint> VALUATION_ASC =
            Comparator.comparing(InvestmentAnalysisResponse.ValuationPoint::year, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(InvestmentAnalysisResponse.ValuationPoint::quarter, Comparator.nullsLast(Comparator.naturalOrder()));

    private final InvestmentAnalysisRepositoryLoader repositoryLoader;
    private final InvestmentAnalysisOverviewBuilder overviewBuilder;
    private final InvestmentAnalysisFinancialSeriesLoader financialSeriesLoader;
    private final InvestmentAnalysisPointMapper pointMapper;

    public InvestmentAnalysisResponse execute(String rawSymbol) {
        return execute(rawSymbol, null, null);
    }

    public InvestmentAnalysisResponse execute(String rawSymbol, Integer annualLimit, Integer quarterlyLimit) {
        Company company = repositoryLoader.resolveCompany(rawSymbol);

        List<FinancialIndicator> indicators = repositoryLoader.loadFinancialIndicators(company.getId(), annualLimit, quarterlyLimit);
        List<CompanyShareholder> shareholders = repositoryLoader.loadShareholders(company.getId());
        List<CompanyDividend> dividends = repositoryLoader.loadCompanyDividends(company.getId(), annualLimit);

        InvestmentAnalysisResponse.Overview overview = overviewBuilder.build(company, indicators);
        List<InvestmentAnalysisResponse.ValuationPoint> valuations = indicators.stream()
                .map(pointMapper::toValuationPoint)
                .sorted(VALUATION_ASC)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        valuations = InvestmentAnalysisLimits.applyValuationYearLimit(valuations, annualLimit, VALUATION_ASC);

        List<InvestmentAnalysisResponse.DividendPoint> dividendPoints = dividends.stream()
                .map(pointMapper::toDividendPoint)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        dividendPoints = InvestmentAnalysisLimits.applyDividendYearLimit(dividendPoints, annualLimit);

        List<InvestmentAnalysisResponse.ShareholderPoint> shareholderPoints = shareholders.stream()
                .map(pointMapper::toShareholderPoint)
                .toList();

        InvestmentAnalysisResponse.FinancialSeries financials =
                financialSeriesLoader.build(company.getId(), company.getCompanyType(), indicators, annualLimit, quarterlyLimit);
        return new InvestmentAnalysisResponse(overview, shareholderPoints, valuations, financials, dividendPoints);
    }

    public InvestmentAnalysisResponse.FinancialSeries executeFinancialSeries(
            String rawSymbol,
            Integer annualLimit,
            Integer quarterlyLimit
    ) {
        Company company = repositoryLoader.resolveCompany(rawSymbol);
        List<FinancialIndicator> indicators = repositoryLoader.loadFinancialIndicators(company.getId(), annualLimit, quarterlyLimit);
        return financialSeriesLoader.build(company.getId(), company.getCompanyType(), indicators, annualLimit, quarterlyLimit);
    }

    public List<InvestmentAnalysisResponse.ValuationPoint> executeValuations(String rawSymbol, Integer annualLimit) {
        Company company = repositoryLoader.resolveCompany(rawSymbol);
        List<FinancialIndicator> indicators = repositoryLoader.loadFinancialIndicatorsForValuations(company.getId(), annualLimit);
        List<InvestmentAnalysisResponse.ValuationPoint> valuations = indicators.stream()
                .map(pointMapper::toValuationPoint)
                .sorted(VALUATION_ASC)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        return InvestmentAnalysisLimits.applyValuationYearLimit(valuations, annualLimit, VALUATION_ASC);
    }

    /**
     * Khi có {@code startDate/endDate} thì backend sẽ lọc valuations theo đúng khoảng ngày,
     * thay vì để FE cắt trên client.
     *
     * <p>
     * Logic lọc "kỳ báo cáo" được mô phỏng theo {@code ValuationChartGroup}:
     * <ul>
     *     <li>{@code showQuarterly == true}: lọc theo "ngày cuối kỳ" (Q1=Mar31, Q2=Jun30, Q3=Sep30, Q4=Dec31).</li>
     *     <li>{@code showQuarterly == false}: chart hiển thị theo năm (FE group theo year và dùng quarter=4),
     *     backend vì vậy trả về tất cả quarter thuộc các year mà Dec31(year) nằm trong [startDate, endDate].</li>
     * </ul>
     * </p>
     */
    public List<InvestmentAnalysisResponse.ValuationPoint> executeValuations(
            String rawSymbol,
            Integer annualLimit,
            String startDateRaw,
            String endDateRaw,
            Boolean showQuarterly
    ) {
        if (startDateRaw == null || endDateRaw == null) {
            return executeValuations(rawSymbol, annualLimit);
        }
        LocalDate startDate = repositoryLoader.parseIsoDate(startDateRaw, "startDate");
        LocalDate endDate = repositoryLoader.parseIsoDate(endDateRaw, "endDate");

        boolean sq = Optional.ofNullable(showQuarterly).orElse(false);
        if (startDate.isAfter(endDate)) {
            LocalDate tmp = startDate;
            startDate = endDate;
            endDate = tmp;
        }

        Company company = repositoryLoader.resolveCompany(rawSymbol);
        List<FinancialIndicator> indicators =
                repositoryLoader.loadFinancialIndicatorsForValuationsByRange(company.getId(), startDate, endDate, sq);

        List<InvestmentAnalysisResponse.ValuationPoint> valuations = indicators.stream()
                .map(pointMapper::toValuationPoint)
                .sorted(VALUATION_ASC)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        return InvestmentAnalysisLimits.applyValuationYearLimit(valuations, annualLimit, VALUATION_ASC);
    }

    public List<InvestmentAnalysisResponse.DividendPoint> executeDividends(String rawSymbol, Integer annualLimit) {
        Company company = repositoryLoader.resolveCompany(rawSymbol);
        List<CompanyDividend> dividends = repositoryLoader.loadCompanyDividends(company.getId(), annualLimit);
        List<InvestmentAnalysisResponse.DividendPoint> dividendPoints = dividends.stream()
                .map(pointMapper::toDividendPoint)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        return InvestmentAnalysisLimits.applyDividendYearLimit(dividendPoints, annualLimit);
    }
}

