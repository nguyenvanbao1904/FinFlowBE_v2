package com.finflow.backend.investment.portfolio.presentation.controller;

import com.finflow.backend.investment.portfolio.application.dto.PortfolioAssetOutput;
import com.finflow.backend.investment.portfolio.application.dto.PortfolioHealthOutput;
import com.finflow.backend.investment.portfolio.application.dto.PortfolioResponseOutput;
import com.finflow.backend.investment.portfolio.application.port.in.GetPortfolioAssetsPort;
import com.finflow.backend.investment.portfolio.application.port.in.GetPortfolioHealthPort;
import com.finflow.backend.investment.portfolio.application.port.in.GetPortfoliosPort;
import com.finflow.backend.investment.portfolio.application.query.GetPortfolioAssetsQuery;
import com.finflow.backend.investment.portfolio.application.query.GetPortfolioHealthQuery;
import com.finflow.backend.investment.portfolio.application.query.GetPortfoliosQuery;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Internal API for AI agent — no JWT; uses {@code X-Internal-Api-Key}.
 * Returns a compact portfolio analysis payload for AI narrative synthesis.
 */
@RestController
@RequestMapping("/api/internal/portfolio")
@RequiredArgsConstructor
public class InternalPortfolioController {

    private static final int HEALTH_QUARTERS = 8;

    private final GetPortfoliosPort getPortfoliosUseCase;
    private final GetPortfolioAssetsPort getPortfolioAssetsUseCase;
    private final GetPortfolioHealthPort getPortfolioHealthUseCase;

    @Operation(summary = "Get portfolio analysis summary for AI agent (internal)")
    @GetMapping("/analysis")
    public ResponseEntity<Map<String, Object>> getPortfolioAnalysis(
            @RequestParam String userId,
            @RequestParam(required = false) String portfolioId
    ) {
        List<PortfolioResponseOutput> portfolios = getPortfoliosUseCase.execute(new GetPortfoliosQuery(userId));
        if (portfolios.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "status", "NO_PORTFOLIO",
                    "message", "Người dùng chưa có danh mục đầu tư."));
        }

        PortfolioResponseOutput portfolio = resolvePortfolio(portfolios, portfolioId);
        List<PortfolioAssetOutput> assets = getPortfolioAssetsUseCase.execute(
                new GetPortfolioAssetsQuery(userId, portfolio.id()));
        PortfolioHealthOutput health = getPortfolioHealthUseCase.execute(
                new GetPortfolioHealthQuery(userId, portfolio.id(), HEALTH_QUARTERS));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "OK");
        result.put("portfolioId", portfolio.id());
        result.put("portfolioName", portfolio.name());
        result.put("cashBalance", portfolio.cashBalance());
        result.put("totalCostBasis", portfolio.totalCostBasis());
        result.put("totalMarketValueClose", portfolio.totalMarketValueClose());
        result.put("totalMarketValue", health.current().totalValueClose());
        result.put("stockMarketValue", health.current().stockValueClose());
        result.put("unrealizedPnL", computeTotalPnL(assets));
        result.put("currentPE", health.current().pe());
        result.put("currentPB", health.current().pb());
        result.put("currentPS", health.current().ps());
        result.put("priceType", health.current().priceType());
        result.put("holdings", buildHoldings(assets, health.current().totalValueClose()));
        result.put("historyQuarters", buildHistory(health.history()));
        result.put("allPortfolios", buildPortfolioList(portfolios));

        return ResponseEntity.ok(result);
    }

    private PortfolioResponseOutput resolvePortfolio(List<PortfolioResponseOutput> portfolios, String portfolioId) {
        if (portfolioId != null && !portfolioId.isBlank()) {
            return portfolios.stream()
                    .filter(p -> p.id().toString().equals(portfolioId.trim()))
                    .findFirst()
                    .orElse(portfolios.get(0));
        }
        return portfolios.get(0);
    }

    private double computeTotalPnL(List<PortfolioAssetOutput> assets) {
        return assets.stream()
                .filter(a -> a.unrealizedPnL() != null)
                .mapToDouble(a -> a.unrealizedPnL().doubleValue())
                .sum();
    }

    private List<Map<String, Object>> buildHoldings(List<PortfolioAssetOutput> assets, double totalMarketValue) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (PortfolioAssetOutput a : assets) {
            Map<String, Object> h = new LinkedHashMap<>();
            h.put("symbol", a.symbol());
            h.put("quantity", a.totalQuantity());
            h.put("averagePrice", a.averagePrice());
            h.put("closePrice", a.closePrice());
            h.put("marketValue", a.marketValueClose());
            h.put("unrealizedPnL", a.unrealizedPnL());
            h.put("unrealizedPnLPct", a.unrealizedPnLPct());
            double weight = (a.marketValueClose() != null && totalMarketValue > 0)
                    ? a.marketValueClose().doubleValue() / totalMarketValue * 100 : 0;
            h.put("weightPct", Math.round(weight * 100.0) / 100.0);
            list.add(h);
        }
        list.sort((x, y) -> {
            double wx = (double) x.getOrDefault("weightPct", 0.0);
            double wy = (double) y.getOrDefault("weightPct", 0.0);
            return Double.compare(wy, wx);
        });
        return list;
    }

    private List<Map<String, Object>> buildHistory(List<PortfolioHealthOutput.HistoryPoint> history) {
        return history.stream().map(p -> {
            Map<String, Object> h = new LinkedHashMap<>();
            h.put("year", p.year());
            h.put("quarter", p.quarter());
            h.put("pe", p.pe());
            h.put("pb", p.pb());
            h.put("roe", p.roe());
            h.put("roa", p.roa());
            h.put("coverage", Math.round(p.coverage() * 100));
            return h;
        }).toList();
    }

    private List<Map<String, Object>> buildPortfolioList(List<PortfolioResponseOutput> portfolios) {
        return portfolios.stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.id());
            m.put("name", p.name());
            m.put("totalCostBasis", p.totalCostBasis());
            m.put("totalMarketValueClose", p.totalMarketValueClose());
            return m;
        }).toList();
    }
}
