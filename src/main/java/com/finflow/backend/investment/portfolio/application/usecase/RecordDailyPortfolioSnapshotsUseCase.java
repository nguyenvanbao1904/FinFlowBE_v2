package com.finflow.backend.investment.portfolio.application.usecase;

import com.finflow.backend.investment.portfolio.application.exception.SnapshotDataNotReadyException;
import com.finflow.backend.investment.portfolio.domain.entity.DailyMarketIndexSnapshot;
import com.finflow.backend.investment.portfolio.domain.entity.DailyPortfolioSnapshot;
import com.finflow.backend.investment.portfolio.domain.entity.Portfolio;
import com.finflow.backend.investment.portfolio.domain.entity.PortfolioAsset;
import com.finflow.backend.investment.portfolio.domain.repository.DailyMarketIndexSnapshotRepository;
import com.finflow.backend.investment.portfolio.domain.repository.DailyPortfolioSnapshotRepository;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioAssetRepository;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioRepository;
import com.finflow.backend.investment.portfolio.infrastructure.VndirectFinfoPriceClient;
import com.finflow.backend.investment.portfolio.infrastructure.VpsMarketPriceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Chốt NAV cuối ngày + snapshot VNINDEX; VPS → Finfo T → Finfo T-1; thiếu giá thì bỏ qua portfolio đó.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecordDailyPortfolioSnapshotsUseCase {

    public static final String BENCHMARK_CODE = "VNINDEX";

    private static final int SHORT_RETRIES = 3;
    private static final long SHORT_RETRY_MS = 2000L;

    private final PortfolioRepository portfolioRepository;
    private final PortfolioAssetRepository portfolioAssetRepository;
    private final DailyPortfolioSnapshotRepository dailyPortfolioSnapshotRepository;
    private final DailyMarketIndexSnapshotRepository dailyMarketIndexSnapshotRepository;
    private final VpsMarketPriceClient vpsMarketPriceClient;
    private final VndirectFinfoPriceClient vndirectFinfoPriceClient;

    @Transactional
    public void execute(LocalDate snapshotDate) {
        DayOfWeek dow = snapshotDate.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            log.debug("Skip daily snapshot on weekend {}", snapshotDate);
            return;
        }

        upsertVnIndex(snapshotDate);

        Map<String, BigDecimal> symbolPriceCache = new HashMap<>();
        for (Portfolio portfolio : portfolioRepository.findAll()) {
            if (dailyPortfolioSnapshotRepository.existsByPortfolioIdAndSnapshotDate(portfolio.getId(), snapshotDate)) {
                continue;
            }
            tryRecordPortfolio(portfolio, snapshotDate, symbolPriceCache);
        }
    }

    private void upsertVnIndex(LocalDate snapshotDate) {
        if (dailyMarketIndexSnapshotRepository.existsByCodeAndSnapshotDate(BENCHMARK_CODE, snapshotDate)) {
            return;
        }
        Optional<BigDecimal> close = fetchWithShortRetries(
                () -> vndirectFinfoPriceClient.getMarketIndexClose(BENCHMARK_CODE, snapshotDate)
        );
        if (close.isEmpty()) {
            throw new SnapshotDataNotReadyException("VNINDEX close not available for " + snapshotDate);
        }
        dailyMarketIndexSnapshotRepository.save(DailyMarketIndexSnapshot.builder()
                .code(BENCHMARK_CODE)
                .snapshotDate(snapshotDate)
                .close(close.get())
                .build());
    }

    private void tryRecordPortfolio(Portfolio portfolio, LocalDate snapshotDate, Map<String, BigDecimal> symbolPriceCache) {
        List<PortfolioAsset> assets = portfolioAssetRepository.findByPortfolio_Id(portfolio.getId());
        List<PortfolioAsset> holdings = assets.stream()
                .filter(a -> a.getTotalQuantity() != null && a.getTotalQuantity().signum() > 0)
                .toList();

        BigDecimal cash = portfolio.getCashBalance() != null ? portfolio.getCashBalance() : BigDecimal.ZERO;

        if (holdings.isEmpty()) {
            saveSnapshot(portfolio.getId(), snapshotDate, cash, cash);
            return;
        }

        Map<String, BigDecimal> prices = new HashMap<>();
        for (PortfolioAsset asset : holdings) {
            String symbol = asset.getSymbol();
            Optional<BigDecimal> px = resolvePrice(symbol, snapshotDate, symbolPriceCache);
            if (px.isEmpty()) {
                log.warn("Skip daily snapshot for portfolio {} on {}: no price for symbol {}", portfolio.getId(), snapshotDate, symbol);
                return;
            }
            prices.put(symbol, px.get());
        }

        BigDecimal stocksValue = BigDecimal.ZERO;
        for (PortfolioAsset asset : holdings) {
            BigDecimal qty = asset.getTotalQuantity();
            BigDecimal px = prices.get(asset.getSymbol());
            stocksValue = stocksValue.add(qty.multiply(px));
        }
        BigDecimal totalNav = cash.add(stocksValue).setScale(2, RoundingMode.HALF_UP);
        saveSnapshot(portfolio.getId(), snapshotDate, totalNav, cash);
    }

    private void saveSnapshot(UUID portfolioId, LocalDate snapshotDate, BigDecimal totalNav, BigDecimal cash) {
        dailyPortfolioSnapshotRepository.save(DailyPortfolioSnapshot.builder()
                .portfolioId(portfolioId)
                .snapshotDate(snapshotDate)
                .totalNav(totalNav)
                .cashBalance(cash.setScale(2, RoundingMode.HALF_UP))
                .build());
    }

    private Optional<BigDecimal> resolvePrice(String symbol, LocalDate snapshotDate, Map<String, BigDecimal> cache) {
        String key = symbol.trim().toUpperCase();
        if (cache.containsKey(key)) {
            return Optional.of(cache.get(key));
        }
        Optional<BigDecimal> vps = fetchWithShortRetries(
                () -> vpsMarketPriceClient.tryFetchCloseFresh(key).map(q -> BigDecimal.valueOf(q.priceVnd()).setScale(2, RoundingMode.HALF_UP))
        );
        if (vps.isPresent()) {
            cache.put(key, vps.get());
            return vps;
        }
        Optional<BigDecimal> finfoT = fetchWithShortRetries(
                () -> vndirectFinfoPriceClient.getStockCloseVnd(key, snapshotDate)
        );
        if (finfoT.isPresent()) {
            cache.put(key, finfoT.get());
            return finfoT;
        }
        Optional<BigDecimal> finfoT1 = fetchWithShortRetries(
                () -> vndirectFinfoPriceClient.getStockCloseVnd(key, snapshotDate.minusDays(1))
        );
        if (finfoT1.isPresent()) {
            cache.put(key, finfoT1.get());
            return finfoT1;
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> fetchWithShortRetries(java.util.function.Supplier<Optional<BigDecimal>> supplier) {
        Optional<BigDecimal> last = Optional.empty();
        for (int i = 0; i < SHORT_RETRIES; i++) {
            last = supplier.get();
            if (last.isPresent()) {
                return last;
            }
            sleepQuiet(SHORT_RETRY_MS);
        }
        return last;
    }

    private static void sleepQuiet(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
