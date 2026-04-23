package com.finflow.backend.investment.portfolio.application.service;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.investment.market_data.api.MarketIndicatorReadApi;
import com.finflow.backend.investment.portfolio.api.MarketPriceApi;
import com.finflow.backend.investment.portfolio.api.MarketPriceQuote;
import com.finflow.backend.investment.market_data.api.MarketIndicatorData;
import com.finflow.backend.investment.portfolio.application.dto.PortfolioHealthOutput;
import com.finflow.backend.investment.portfolio.domain.entity.Portfolio;
import com.finflow.backend.investment.portfolio.domain.entity.PortfolioAsset;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioAssetRepository;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioRepository;
import com.finflow.backend.investment.portfolio.exception.PortfolioErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core P/E, P/B, P/S and history math for a portfolio. Invoked from
 * {@link com.finflow.backend.investment.portfolio.application.usecase.GetPortfolioHealthUseCase}
 * and {@link com.finflow.backend.investment.portfolio.application.usecase.GetPortfolioVsMarketUseCase}
 * so use cases do not call each other (ArchUnit).
 *
 * <p><strong>Wall Street–style weighting:</strong>
 * <ul>
 *   <li>P/E, P/B current: harmonic mean (market weight = qty × close)</li>
 *   <li>P/S current: arithmetic mean (market weight)</li>
 *   <li>History: same formulas with cost weight = qty × avg price</li>
 *   <li>Coverage threshold: if sum of valid weights &lt; 0.5 → metric is null</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class PortfolioHealthComputationService {

    private static final double MIN_COVERAGE = 0.5;

    private final PortfolioRepository portfolioRepository;
    private final PortfolioAssetRepository assetRepository;
    private final MarketIndicatorReadApi marketIndicatorReadApi;
    private final MarketPriceApi marketPriceApi;

    /**
     * Loads portfolio data, prices, and indicators; returns aggregated health view.
     */
    public PortfolioHealthOutput compute(String userId, UUID portfolioId, int quartersLimit) {
        int safeQuartersLimit = Math.max(1, Math.min(quartersLimit, 40));

        Portfolio portfolio = portfolioRepository
                .findByIdAndUserId(portfolioId, userId)
                .orElseThrow(() -> new AppException(PortfolioErrorCode.PORTFOLIO_NOT_FOUND));

        List<PortfolioAsset> assets = assetRepository.findByPortfolio_IdAndPortfolio_UserId(portfolioId, userId);
        if (assets.isEmpty()) {
            return emptyResult(portfolio);
        }

        List<String> symbols = assets.stream().map(PortfolioAsset::getSymbol).toList();
        Map<String, MarketPriceQuote> closePrices = marketPriceApi.getClosePrices(symbols);

        List<MarketIndicatorData> allIndicators = marketIndicatorReadApi.findAllByCompanyIds(symbols);

        Map<String, List<MarketIndicatorData>> bySymbol = allIndicators.stream()
                .collect(Collectors.groupingBy(
                        MarketIndicatorData::companyId,
                        Collectors.collectingAndThen(Collectors.toList(),
                                list -> list.stream()
                                        .sorted(Comparator.comparingInt(MarketIndicatorData::year).reversed()
                                                .thenComparing(Comparator.comparingInt(MarketIndicatorData::quarter).reversed()))
                                        .limit(safeQuartersLimit)
                                        .toList())
                ));

        OptionalInt maxYear = allIndicators.stream().mapToInt(MarketIndicatorData::year).max();
        OptionalInt maxQ = allIndicators.stream()
                .filter(i -> maxYear.isPresent() && i.year() == maxYear.getAsInt())
                .mapToInt(MarketIndicatorData::quarter).max();
        int latestYear = maxYear.orElse(0);
        int latestQuarter = maxQ.orElse(0);

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

        PortfolioHealthOutput.CurrentSnapshot current =
                buildCurrentSnapshot(assets, closePrices, bySymbol, marketWeight, costWeight, portfolio);

        List<PortfolioHealthOutput.HistoryPoint> history = buildHistory(assets, bySymbol, costWeight, safeQuartersLimit);

        return new PortfolioHealthOutput(latestYear, latestQuarter, current, history);
    }

    private PortfolioHealthOutput.CurrentSnapshot buildCurrentSnapshot(
            List<PortfolioAsset> assets,
            Map<String, MarketPriceQuote> closePrices,
            Map<String, List<MarketIndicatorData>> bySymbol,
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

        Map<String, MarketIndicatorData> latest = new HashMap<>();
        for (Map.Entry<String, List<MarketIndicatorData>> e : bySymbol.entrySet()) {
            if (!e.getValue().isEmpty()) {
                latest.put(e.getKey(), e.getValue().get(0));
            }
        }

        Double pe = null;
        Double pb = null;
        Double ps = null;
        if (hasSufficientClose) {
            pe = harmonicMean(assets.stream().map(PortfolioAsset::getSymbol).toList(),
                    marketWeight, latest, fi -> safeDouble(fi.pe()));
            pb = harmonicMean(assets.stream().map(PortfolioAsset::getSymbol).toList(),
                    marketWeight, latest, fi -> safeDouble(fi.pb()));
            ps = arithmeticMean(assets.stream().map(PortfolioAsset::getSymbol).toList(),
                    marketWeight, latest, fi -> safeDouble(fi.ps()));
        }

        return new PortfolioHealthOutput.CurrentSnapshot(totalValueClose, stockValueClose, cashBalance, pe, pb, ps, priceType);
    }

    private List<PortfolioHealthOutput.HistoryPoint> buildHistory(
            List<PortfolioAsset> assets,
            Map<String, List<MarketIndicatorData>> bySymbol,
            Map<String, Double> costWeight,
            int quartersLimit
    ) {
        record Quarter(int year, int quarter) implements Comparable<Quarter> {
            public int compareTo(Quarter o) {
                return year != o.year ? Integer.compare(year, o.year) : Integer.compare(quarter, o.quarter);
            }
        }

        Set<Quarter> quarters = new TreeSet<>();
        for (List<MarketIndicatorData> list : bySymbol.values()) {
            list.forEach(fi -> quarters.add(new Quarter(fi.year(), fi.quarter())));
        }

        List<Quarter> sortedQuarters = quarters.stream()
                .sorted(Comparator.reverseOrder())
                .limit(quartersLimit)
                .sorted()
                .toList();

        List<String> symbols = assets.stream().map(PortfolioAsset::getSymbol).toList();
        List<PortfolioHealthOutput.HistoryPoint> points = new ArrayList<>();

        for (Quarter q : sortedQuarters) {
            Map<String, MarketIndicatorData> snap = new HashMap<>();
            for (String sym : symbols) {
                List<MarketIndicatorData> list = bySymbol.getOrDefault(sym, List.of());
                list.stream()
                        .filter(fi -> fi.year() == q.year() && fi.quarter() == q.quarter())
                        .findFirst()
                        .ifPresent(fi -> snap.put(sym, fi));
            }

            double coverage = symbols.stream()
                    .filter(s -> {
                        MarketIndicatorData fi = snap.get(s);
                        return fi != null && (
                                positiveDouble(fi.pe()) ||
                                        positiveDouble(fi.pb()) ||
                                        safeDouble(fi.ps()) != null ||
                                        safeDouble(fi.roe()) != null ||
                                        safeDouble(fi.roa()) != null
                        );
                    })
                    .mapToDouble(s -> costWeight.getOrDefault(s, 0.0))
                    .sum();

            Double pe = harmonicMeanSnap(symbols, costWeight, snap, fi -> safeDouble(fi.pe()));
            Double pb = harmonicMeanSnap(symbols, costWeight, snap, fi -> safeDouble(fi.pb()));
            Double ps = arithmeticMeanSnap(symbols, costWeight, snap, fi -> safeDouble(fi.ps()));
            Double roe = arithmeticMeanSnap(symbols, costWeight, snap, fi -> safeDouble(fi.roe()));
            Double roa = arithmeticMeanSnap(symbols, costWeight, snap, fi -> safeDouble(fi.roa()));

            points.add(new PortfolioHealthOutput.HistoryPoint(q.year(), q.quarter(), pe, pb, ps, roe, roa, coverage));
        }

        return points;
    }

    @FunctionalInterface
    private interface IndicatorExtractor {
        Double extract(MarketIndicatorData fi);
    }

    private Double harmonicMean(
            List<String> symbols,
            Map<String, Double> weights,
            Map<String, MarketIndicatorData> indicators,
            IndicatorExtractor extractor
    ) {
        double sumInverseWeighted = 0;
        double sumWeight = 0;
        for (String s : symbols) {
            MarketIndicatorData fi = indicators.get(s);
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
            List<String> symbols, Map<String, Double> weights,
            Map<String, MarketIndicatorData> snap, IndicatorExtractor extractor
    ) {
        return harmonicMean(symbols, weights, snap, extractor);
    }

    private Double arithmeticMean(
            List<String> symbols,
            Map<String, Double> weights,
            Map<String, MarketIndicatorData> indicators,
            IndicatorExtractor extractor
    ) {
        double sumWeighted = 0;
        double sumWeight = 0;
        for (String s : symbols) {
            MarketIndicatorData fi = indicators.get(s);
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
            List<String> symbols, Map<String, Double> weights,
            Map<String, MarketIndicatorData> snap, IndicatorExtractor extractor
    ) {
        return arithmeticMean(symbols, weights, snap, extractor);
    }

    private Double safeDouble(BigDecimal bd) {
        return bd != null ? bd.doubleValue() : null;
    }

    private boolean positiveDouble(BigDecimal bd) {
        return bd != null && bd.doubleValue() > 0;
    }

    private PortfolioHealthOutput emptyResult(Portfolio portfolio) {
        PortfolioHealthOutput.CurrentSnapshot empty = new PortfolioHealthOutput.CurrentSnapshot(
                portfolio.getCashBalance().doubleValue(), 0,
                portfolio.getCashBalance().doubleValue(), null, null, null, "INSUFFICIENT"
        );
        return new PortfolioHealthOutput(0, 0, empty, List.of());
    }
}
