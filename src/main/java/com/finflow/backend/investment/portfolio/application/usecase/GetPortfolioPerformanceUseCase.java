package com.finflow.backend.investment.portfolio.application.usecase;

import com.finflow.backend.investment.portfolio.domain.entity.DailyMarketIndexSnapshot;
import com.finflow.backend.investment.portfolio.domain.entity.DailyPortfolioSnapshot;
import com.finflow.backend.investment.portfolio.domain.repository.DailyMarketIndexSnapshotRepository;
import com.finflow.backend.investment.portfolio.domain.repository.DailyPortfolioSnapshotRepository;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioRepository;
import com.finflow.backend.investment.portfolio.presentation.response.PerformanceSeriesPointResponse;
import com.finflow.backend.investment.portfolio.presentation.response.PortfolioPerformanceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPortfolioPerformanceUseCase {

    private static final String DEFAULT_BENCHMARK_CODE = RecordDailyPortfolioSnapshotsUseCase.BENCHMARK_CODE;

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final PortfolioRepository portfolioRepository;
    private final DailyPortfolioSnapshotRepository dailyPortfolioSnapshotRepository;
    private final DailyMarketIndexSnapshotRepository dailyMarketIndexSnapshotRepository;

    @Transactional(readOnly = true)
    public PortfolioPerformanceResponse execute(
            String userId,
            UUID portfolioId,
            PerformanceRange range,
            LocalDate startOverride,
            LocalDate endOverride
    ) {
        portfolioRepository.findByIdAndUserId(portfolioId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found"));

        LocalDate end = endOverride != null ? endOverride : LocalDate.now(VN_ZONE);
        LocalDate start = startOverride != null ? startOverride : range.resolveStart(end);

        if (start.isAfter(end)) {
            return new PortfolioPerformanceResponse(start, end, DEFAULT_BENCHMARK_CODE, List.of(), List.of());
        }

        List<DailyPortfolioSnapshot> navRows = dailyPortfolioSnapshotRepository
                .findByPortfolioIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(portfolioId, start, end);
        List<DailyMarketIndexSnapshot> idxRows = dailyMarketIndexSnapshotRepository
                .findByCodeAndSnapshotDateBetweenOrderBySnapshotDateAsc(
                        DEFAULT_BENCHMARK_CODE,
                        start,
                        end
                );

        return new PortfolioPerformanceResponse(
                start,
                end,
                DEFAULT_BENCHMARK_CODE,
                toNavPoints(navRows),
                toIndexPoints(idxRows)
        );
    }

    private List<PerformanceSeriesPointResponse> toNavPoints(List<DailyPortfolioSnapshot> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }
        BigDecimal anchor = rows.getFirst().getTotalNav();
        List<PerformanceSeriesPointResponse> out = new ArrayList<>();
        for (DailyPortfolioSnapshot r : rows) {
            BigDecimal v = r.getTotalNav();
            out.add(new PerformanceSeriesPointResponse(r.getSnapshotDate(), v, pctFromAnchor(anchor, v)));
        }
        return out;
    }

    private List<PerformanceSeriesPointResponse> toIndexPoints(List<DailyMarketIndexSnapshot> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }
        BigDecimal anchor = rows.getFirst().getClose();
        List<PerformanceSeriesPointResponse> out = new ArrayList<>();
        for (DailyMarketIndexSnapshot r : rows) {
            BigDecimal v = r.getClose();
            out.add(new PerformanceSeriesPointResponse(r.getSnapshotDate(), v, pctFromAnchor(anchor, v)));
        }
        return out;
    }

    private static BigDecimal pctFromAnchor(BigDecimal anchor, BigDecimal value) {
        if (anchor == null || value == null) {
            return null;
        }
        if (anchor.signum() == 0) {
            return null;
        }
        return value.subtract(anchor)
                .divide(anchor, 8, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(4, RoundingMode.HALF_UP);
    }

    public enum PerformanceRange {
        ONE_MONTH("1M"),
        THREE_MONTHS("3M"),
        SIX_MONTHS("6M"),
        ONE_YEAR("1Y"),
        YEAR_TO_DATE("YTD"),
        MAX("MAX");

        private final String param;

        PerformanceRange(String param) {
            this.param = param;
        }

        public static PerformanceRange fromParam(String raw) {
            if (raw == null || raw.isBlank()) {
                return ONE_YEAR;
            }
            String u = raw.trim().toUpperCase();
            for (PerformanceRange r : values()) {
                if (r.param.equals(u)) {
                    return r;
                }
            }
            return ONE_YEAR;
        }

        public LocalDate resolveStart(LocalDate end) {
            return switch (this) {
                case ONE_MONTH -> end.minusMonths(1);
                case THREE_MONTHS -> end.minusMonths(3);
                case SIX_MONTHS -> end.minusMonths(6);
                case ONE_YEAR -> end.minusYears(1);
                case YEAR_TO_DATE -> LocalDate.of(end.getYear(), 1, 1);
                case MAX -> end.minusYears(10);
            };
        }
    }
}
