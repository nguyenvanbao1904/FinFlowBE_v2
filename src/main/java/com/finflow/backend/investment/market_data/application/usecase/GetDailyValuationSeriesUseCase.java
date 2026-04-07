package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.investment.market_data.application.service.InvestmentAnalysisRepositoryLoader;
import com.finflow.backend.investment.market_data.application.service.InvestmentFinancialUtils;
import com.finflow.backend.investment.market_data.domain.entity.Company;
import com.finflow.backend.investment.market_data.domain.entity.BankIncomeStatement;
import com.finflow.backend.investment.market_data.domain.entity.FinancialIndicator;
import com.finflow.backend.investment.market_data.domain.entity.NonBankIncomeStatement;
import com.finflow.backend.investment.market_data.exception.MarketDataErrorCode;
import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;
import com.finflow.backend.investment.portfolio.infrastructure.VndirectFinfoPriceClient;
import com.finflow.backend.investment.portfolio.infrastructure.VndirectFinfoPriceClient.StockDailyClose;
import com.finflow.backend.investment.portfolio.infrastructure.VpsMarketPriceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

/**
 * P/E, P/B, P/S theo từng ngày: giá Finfo (range), EPS/BVPS/CPLH từ chỉ số; P/S = giá / (mẫu TTM trên CP):
 * non-bank = doanh thu thuần TTM; NH = (thu nhập lãi thuần + dịch vụ + khác) TTM.
 */
@Component
@RequiredArgsConstructor
public class GetDailyValuationSeriesUseCase {

    /** Cùng mốc tối thiểu với bộ chọn quý trên FE (năm ≥ 2010). */
    private static final LocalDate DAILY_VALUATION_MIN_INCLUSIVE = LocalDate.of(2010, 1, 1);

    private final InvestmentAnalysisRepositoryLoader repositoryLoader;
    private final VndirectFinfoPriceClient vndirectFinfoPriceClient;
    private final VpsMarketPriceClient vpsMarketPriceClient;

    @Transactional(readOnly = true)
    public List<InvestmentAnalysisResponse.DailyValuationPoint> execute(
            String rawSymbol,
            String startDateRaw,
            String endDateRaw
    ) {
        Company company = repositoryLoader.resolveCompany(rawSymbol);
        LocalDate parsedStart = repositoryLoader.parseIsoDate(startDateRaw, "startDate");
        LocalDate parsedEnd = repositoryLoader.parseIsoDate(endDateRaw, "endDate");
        LocalDate rangeStart = parsedStart.isAfter(parsedEnd) ? parsedEnd : parsedStart;
        LocalDate rangeEnd = parsedStart.isAfter(parsedEnd) ? parsedStart : parsedEnd;
        if (rangeStart.isBefore(DAILY_VALUATION_MIN_INCLUSIVE)) {
            throw new AppException(MarketDataErrorCode.DAILY_VALUATION_START_BEFORE_MIN);
        }
        long span = ChronoUnit.DAYS.between(rangeStart, rangeEnd) + 1;
        long maxSpanDays = ChronoUnit.DAYS.between(DAILY_VALUATION_MIN_INCLUSIVE, LocalDate.now()) + 1;
        if (span > maxSpanDays) {
            throw new AppException(MarketDataErrorCode.DAILY_VALUATION_RANGE_TOO_LONG);
        }

        List<FinancialIndicator> indicatorsAsc = repositoryLoader.loadAllFinancialIndicatorsAsc(company.getId());
        List<NonBankIncomeStatement> incomesAsc = repositoryLoader.loadAllNonBankIncomesAsc(company.getId());
        boolean isBank = "BANK".equalsIgnoreCase(Optional.ofNullable(company.getCompanyType()).orElse("").trim());
        List<BankIncomeStatement> bankIncomesAsc = isBank ? repositoryLoader.loadAllBankIncomesAsc(company.getId()) : List.of();

        List<StockDailyClose> finfoRows = vndirectFinfoPriceClient.listStockClosesInRange(company.getId(), rangeStart, rangeEnd);
        TreeMap<LocalDate, BigDecimal> priceByDay = new TreeMap<>();
        for (StockDailyClose row : finfoRows) {
            priceByDay.put(row.date(), row.closeVnd());
        }

        if (!priceByDay.containsKey(rangeEnd) && !rangeEnd.isAfter(LocalDate.now())) {
            vpsMarketPriceClient.tryFetchCloseFresh(company.getId()).ifPresent(q ->
                    priceByDay.put(rangeEnd, BigDecimal.valueOf(q.priceVnd()).setScale(2, RoundingMode.HALF_UP))
            );
        }

        List<InvestmentAnalysisResponse.DailyValuationPoint> out = new ArrayList<>();
        for (LocalDate d : priceByDay.keySet()) {
            BigDecimal pxBd = priceByDay.get(d);
            if (pxBd == null) {
                continue;
            }
            double price = pxBd.doubleValue();
            FinancialIndicator indLatest = InvestmentFinancialUtils.latestIndicatorAsOf(indicatorsAsc, d);
            if (indLatest == null) {
                continue;
            }

            Double epsTtm = InvestmentFinancialUtils.epsTtmAsOf(indicatorsAsc, d);
            Double pe = epsTtm != null && epsTtm > 0 ? price / epsTtm : null;

            Double bvps = indLatest.getBvps() != null ? indLatest.getBvps().doubleValue() : null;
            Double pb = bvps != null && bvps > 0 ? price / bvps : null;

            // P/S: non-bank = doanh thu thuần TTM; NH = tổng thu nhập hoạt động (NII + phí + khác) TTM / CP.
            Double ps = null;
            Double ttmNumerator = isBank
                    ? InvestmentFinancialUtils.bankTopLineTtmAsOf(bankIncomesAsc, d)
                    : InvestmentFinancialUtils.netRevenueTtmAsOf(incomesAsc, d);
            Double shares = indLatest.getCplh() != null
                    ? InvestmentFinancialUtils.absoluteSharesFromCplh(indLatest.getCplh().doubleValue())
                    : null;
            if (shares != null && ttmNumerator != null && shares > 0 && ttmNumerator > 0) {
                double perShare = ttmNumerator / shares;
                if (perShare > 0) {
                    ps = price / perShare;
                }
            }

            out.add(new InvestmentAnalysisResponse.DailyValuationPoint(
                    d.toString(),
                    pe == null ? null : round4(pe),
                    pb == null ? null : round4(pb),
                    ps == null ? null : round4(ps)
            ));
        }
        return out;
    }

    private static Double round4(double v) {
        return BigDecimal.valueOf(v).setScale(4, RoundingMode.HALF_UP).doubleValue();
    }
}
