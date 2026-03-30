package com.finflow.backend.investment.portfolio.application.usecase;

import com.finflow.backend.investment.market_data.domain.entity.FinancialIndicator;
import com.finflow.backend.investment.market_data.domain.repository.FinancialIndicatorRepository;
import com.finflow.backend.investment.portfolio.domain.entity.Portfolio;
import com.finflow.backend.investment.portfolio.domain.entity.PortfolioAsset;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioAssetRepository;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioRepository;
import com.finflow.backend.investment.portfolio.infrastructure.VpsMarketPriceClient;
import com.finflow.backend.investment.portfolio.infrastructure.VpsMarketPriceClient.MarketPriceQuote;
import com.finflow.backend.investment.portfolio.infrastructure.VpsMarketPriceClient.PriceSource;
import com.finflow.backend.investment.portfolio.presentation.response.PortfolioHealthResponse;
import com.finflow.backend.investment.portfolio.presentation.response.PortfolioHealthResponse.CurrentSnapshot;
import com.finflow.backend.investment.portfolio.presentation.response.PortfolioHealthResponse.HistoryPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Tính toán "sức khỏe" danh mục: định giá P/E, P/B, P/S theo quý.
 *
 * <p><strong>Công thức chuẩn Wall Street:</strong>
 * <ul>
 *   <li>P/E current  = harmonic mean (market weight = qty × closePrice)</li>
 *   <li>P/B current  = harmonic mean (market weight)</li>
 *   <li>P/S current  = arithmetic mean (market weight)</li>
 *   <li>History: cùng công thức nhưng dùng cost weight = qty × avgPrice</li>
 *   <li>Coverage threshold: nếu sumWeightValid &lt; 0.5 → metric = null</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetPortfolioHealthUseCase {

    private static final double MIN_COVERAGE = 0.5;

    private final PortfolioRepository portfolioRepository;
    private final PortfolioAssetRepository assetRepository;
    private final FinancialIndicatorRepository indicatorRepository;
    private final VpsMarketPriceClient vpsClient;

    @Transactional(readOnly = true)
    public PortfolioHealthResponse execute(String userId, UUID portfolioId, int quartersLimit) {
        int safeQuartersLimit = Math.max(1, Math.min(quartersLimit, 40));

        // --- Load portfolio & validate ownership ---
        Portfolio portfolio = portfolioRepository
                .findByIdAndUserId(portfolioId, userId)
                .orElseThrow(() -> new NoSuchElementException("Portfolio not found: " + portfolioId));

        List<PortfolioAsset> assets = assetRepository.findByPortfolio_IdAndPortfolio_UserId(portfolioId, userId);
        if (assets.isEmpty()) {
            return emptyResponse(portfolio);
        }

        // --- Asset metadata ---
        List<String> symbols = assets.stream().map(PortfolioAsset::getSymbol).toList();
        Map<String, MarketPriceQuote> closePrices = vpsClient.getClosePrices(symbols);

        // FinancialIndicator.companyId == symbol (see Company.id = ticker)
        List<FinancialIndicator> allIndicators =
                indicatorRepository.findByCompanyIdInOrderByCompanyIdAscYearDescQuarterDesc(symbols);

        // Group indicators: symbol → list (newest first), limited by quartersLimit per symbol
        Map<String, List<FinancialIndicator>> bySymbol = allIndicators.stream()
                .collect(Collectors.groupingBy(
                        FinancialIndicator::getCompanyId,
                        Collectors.collectingAndThen(Collectors.toList(),
                                list -> list.stream()
                                        .sorted(Comparator.comparingInt(FinancialIndicator::getYear).reversed()
                                                .thenComparing(Comparator.comparingInt(FinancialIndicator::getQuarter).reversed()))
                                        .limit(safeQuartersLimit)
                                        .toList())
                ));

        // --- Latest quarter across all symbols ---
        OptionalInt maxYear = allIndicators.stream().mapToInt(FinancialIndicator::getYear).max();
        OptionalInt maxQ = allIndicators.stream()
                .filter(i -> maxYear.isPresent() && i.getYear() == maxYear.getAsInt())
                .mapToInt(FinancialIndicator::getQuarter).max();
        int latestYear = maxYear.orElse(0);
        int latestQuarter = maxQ.orElse(0);

        // --- Weights by cost (history) ---
        double totalCost = assets.stream()
                .mapToDouble(a -> a.getTotalQuantity().doubleValue() * a.getAveragePrice().doubleValue())
                .sum();
        Map<String, Double> costWeight = new HashMap<>();
        for (PortfolioAsset a : assets) {
            double w = totalCost > 0
                    ? (a.getTotalQuantity().doubleValue() * a.getAveragePrice().doubleValue()) / totalCost
                    : 0;
            costWeight.put(a.getSymbol(), w);
        }

        // --- Weights by market/close price (current) ---
        double totalMarket = assets.stream()
                .mapToDouble(a -> {
                    MarketPriceQuote quote = closePrices.get(a.getSymbol());
                    return quote != null ? a.getTotalQuantity().doubleValue() * quote.priceVnd() : 0;
                }).sum();
        Map<String, Double> marketWeight = new HashMap<>();
        for (PortfolioAsset a : assets) {
            MarketPriceQuote quote = closePrices.get(a.getSymbol());
            double w = (quote != null && totalMarket > 0)
                    ? (a.getTotalQuantity().doubleValue() * quote.priceVnd()) / totalMarket
                    : 0;
            marketWeight.put(a.getSymbol(), w);
        }

        // --- Current snapshot (market weights) ---
        CurrentSnapshot current =
                buildCurrentSnapshot(assets, closePrices, bySymbol, marketWeight, costWeight, portfolio);

        // --- History (cost weights, grouped by year+quarter) ---
        List<HistoryPoint> history = buildHistory(assets, bySymbol, costWeight, safeQuartersLimit);

        return new PortfolioHealthResponse(latestYear, latestQuarter, current, history);
    }

    // -------------------------------------------------------------------------
    // Current snapshot
    // -------------------------------------------------------------------------

    private CurrentSnapshot buildCurrentSnapshot(
            List<PortfolioAsset> assets,
            Map<String, MarketPriceQuote> closePrices,
            Map<String, List<FinancialIndicator>> bySymbol,
            Map<String, Double> marketWeight,
            Map<String, Double> costWeight,
            Portfolio portfolio
    ) {
        double closeCoverage = assets.stream()
                .filter(a -> closePrices.containsKey(a.getSymbol()))
                .mapToDouble(a -> costWeight.getOrDefault(a.getSymbol(), 0.0))
                .sum();
        boolean hasSufficientClose = closeCoverage >= MIN_COVERAGE;

        String priceType = hasSufficientClose ? "CLOSE" : "INSUFFICIENT";

        double stockValueClose = assets.stream()
                .mapToDouble(a -> {
                    MarketPriceQuote quote = closePrices.get(a.getSymbol());
                    return quote != null ? a.getTotalQuantity().doubleValue() * quote.priceVnd() : 0;
                }).sum();
        double cashBalance = portfolio.getCashBalance().doubleValue();
        double totalValueClose = stockValueClose + cashBalance;

        // Latest indicator per symbol for current PE/PB/PS
        Map<String, FinancialIndicator> latest = new HashMap<>();
        for (Map.Entry<String, List<FinancialIndicator>> e : bySymbol.entrySet()) {
            if (!e.getValue().isEmpty()) latest.put(e.getKey(), e.getValue().get(0));
        }

        Double pe = null;
        Double pb = null;
        Double ps = null;
        if (hasSufficientClose) {
            pe = harmonicMean(assets.stream().map(PortfolioAsset::getSymbol).toList(),
                    marketWeight, latest, fi -> safeDouble(fi.getPe()));
            pb = harmonicMean(assets.stream().map(PortfolioAsset::getSymbol).toList(),
                    marketWeight, latest, fi -> safeDouble(fi.getPb()));
            ps = arithmeticMean(assets.stream().map(PortfolioAsset::getSymbol).toList(),
                    marketWeight, latest, fi -> safeDouble(fi.getPs()));
        }

        return new CurrentSnapshot(totalValueClose, stockValueClose, cashBalance, pe, pb, ps, priceType);
    }

    // -------------------------------------------------------------------------
    // History
    // -------------------------------------------------------------------------

    private List<HistoryPoint> buildHistory(
            List<PortfolioAsset> assets,
            Map<String, List<FinancialIndicator>> bySymbol,
            Map<String, Double> costWeight,
            int quartersLimit
    ) {
        // Collect all (year, quarter) keys across all indicators
        record Quarter(int year, int quarter) implements Comparable<Quarter> {
            public int compareTo(Quarter o) {
                return year != o.year ? Integer.compare(year, o.year) : Integer.compare(quarter, o.quarter);
            }
        }

        Set<Quarter> quarters = new TreeSet<>();
        for (List<FinancialIndicator> list : bySymbol.values()) {
            list.forEach(fi -> quarters.add(new Quarter(fi.getYear(), fi.getQuarter())));
        }

        // Take most recent quartersLimit quarters
        List<Quarter> sortedQuarters = quarters.stream()
                .sorted(Comparator.reverseOrder())
                .limit(quartersLimit)
                .sorted()
                .toList();

        List<String> symbols = assets.stream().map(PortfolioAsset::getSymbol).toList();
        List<HistoryPoint> points = new ArrayList<>();

        for (Quarter q : sortedQuarters) {
            // Build snapshot: symbol → indicator for this quarter
            Map<String, FinancialIndicator> snap = new HashMap<>();
            for (String sym : symbols) {
                List<FinancialIndicator> list = bySymbol.getOrDefault(sym, List.of());
                list.stream()
                        .filter(fi -> fi.getYear() == q.year() && fi.getQuarter() == q.quarter())
                        .findFirst()
                        .ifPresent(fi -> snap.put(sym, fi));
            }

            // Coverage = sum of cost weights for symbols có ít nhất 1 metric hợp lệ (PE/PB/PS/ROE/ROA)
            double coverage = symbols.stream()
                    .filter(s -> {
                        FinancialIndicator fi = snap.get(s);
                        return fi != null && (
                                positiveDouble(fi.getPe()) ||
                                        positiveDouble(fi.getPb()) ||
                                        safeDouble(fi.getPs()) != null ||
                                        safeDouble(fi.getRoe()) != null ||
                                        safeDouble(fi.getRoa()) != null
                        );
                    })
                    .mapToDouble(s -> costWeight.getOrDefault(s, 0.0))
                    .sum();

            Double pe = harmonicMeanSnap(symbols, costWeight, snap, fi -> safeDouble(fi.getPe()));
            Double pb = harmonicMeanSnap(symbols, costWeight, snap, fi -> safeDouble(fi.getPb()));
            Double ps = arithmeticMeanSnap(symbols, costWeight, snap, fi -> safeDouble(fi.getPs()));
            Double roe = arithmeticMeanSnap(symbols, costWeight, snap, fi -> safeDouble(fi.getRoe()));
            Double roa = arithmeticMeanSnap(symbols, costWeight, snap, fi -> safeDouble(fi.getRoa()));

            points.add(new HistoryPoint(q.year(), q.quarter(), pe, pb, ps, roe, roa, coverage));
        }

        return points;
    }

    // -------------------------------------------------------------------------
    // Math helpers
    // -------------------------------------------------------------------------

    @FunctionalInterface
    private interface IndicatorExtractor {
        Double extract(FinancialIndicator fi);
    }

    /** Harmonic mean: 1 / Σ(w_i / v_i). Returns null if coverage < threshold. */
    private Double harmonicMean(
            List<String> symbols,
            Map<String, Double> weights,
            Map<String, FinancialIndicator> indicators,
            IndicatorExtractor extractor
    ) {
        double sumInverseWeighted = 0;
        double sumWeight = 0;
        for (String s : symbols) {
            FinancialIndicator fi = indicators.get(s);
            if (fi == null) continue;
            Double v = extractor.extract(fi);
            if (v == null || v <= 0) continue;
            double w = weights.getOrDefault(s, 0.0);
            sumInverseWeighted += w / v;
            sumWeight += w;
        }
        if (sumWeight < MIN_COVERAGE || sumInverseWeighted == 0) return null;
        return sumWeight / sumInverseWeighted;
    }

    private Double harmonicMeanSnap(
            List<String> symbols,
            Map<String, Double> weights,
            Map<String, FinancialIndicator> snap,
            IndicatorExtractor extractor
    ) {
        return harmonicMean(symbols, weights, snap, extractor);
    }

    /** Weighted arithmetic mean. Returns null if coverage < threshold. */
    private Double arithmeticMean(
            List<String> symbols,
            Map<String, Double> weights,
            Map<String, FinancialIndicator> indicators,
            IndicatorExtractor extractor
    ) {
        double sumWeighted = 0;
        double sumWeight = 0;
        for (String s : symbols) {
            FinancialIndicator fi = indicators.get(s);
            if (fi == null) continue;
            Double v = extractor.extract(fi);
            if (v == null) continue;
            double w = weights.getOrDefault(s, 0.0);
            sumWeighted += w * v;
            sumWeight += w;
        }
        if (sumWeight < MIN_COVERAGE) return null;
        return sumWeighted / sumWeight;
    }

    private Double arithmeticMeanSnap(
            List<String> symbols,
            Map<String, Double> weights,
            Map<String, FinancialIndicator> snap,
            IndicatorExtractor extractor
    ) {
        return arithmeticMean(symbols, weights, snap, extractor);
    }

    private Double safeDouble(BigDecimal bd) {
        return bd != null ? bd.doubleValue() : null;
    }

    private boolean positiveDouble(BigDecimal bd) {
        return bd != null && bd.doubleValue() > 0;
    }

    private PortfolioHealthResponse emptyResponse(Portfolio portfolio) {
        CurrentSnapshot empty = new CurrentSnapshot(
                portfolio.getCashBalance().doubleValue(),
                0,
                portfolio.getCashBalance().doubleValue(),
                null, null, null, "INSUFFICIENT"
        );
        return new PortfolioHealthResponse(0, 0, empty, List.of());
    }
}
