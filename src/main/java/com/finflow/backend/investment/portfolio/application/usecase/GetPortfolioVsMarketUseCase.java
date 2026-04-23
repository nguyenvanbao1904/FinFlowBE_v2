package com.finflow.backend.investment.portfolio.application.usecase;

import com.finflow.backend.investment.portfolio.application.port.in.GetPortfolioVsMarketPort;
import com.finflow.backend.investment.portfolio.application.query.GetPortfolioVsMarketQuery;

import com.finflow.backend.investment.portfolio.application.dto.PortfolioMarketBenchmarkOutput;
import com.finflow.backend.investment.portfolio.api.StockRatiosApi;
import com.finflow.backend.investment.portfolio.application.dto.PortfolioHealthOutput;
import com.finflow.backend.investment.portfolio.application.service.PortfolioHealthComputationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetPortfolioVsMarketUseCase implements GetPortfolioVsMarketPort {

    private static final String PE_CODE = "81007";
    private static final String PB_CODE = "81013";
    private static final String PS_CODE = "81017";
    private static final String ROE_CODE = "82008";
    private static final String ROA_CODE = "82006";

    private final PortfolioHealthComputationService portfolioHealthComputationService;
    private final StockRatiosApi stockRatiosApi;

    @Transactional(readOnly = true)
    @Override
    public PortfolioMarketBenchmarkOutput execute(GetPortfolioVsMarketQuery request) {
        String userId = request.userId();
        UUID portfolioId = request.portfolioId();
        String benchmarkCode = request.benchmarkCode();
        String code = benchmarkCode == null || benchmarkCode.isBlank()
                ? "VNINDEX"
                : benchmarkCode.trim().toUpperCase();

        PortfolioHealthOutput health = portfolioHealthComputationService.compute(userId, portfolioId, 20);
        Map<String, Double> benchmark = stockRatiosApi.getLatestRatios(
                code, List.of(PE_CODE, PB_CODE, PS_CODE, ROE_CODE, ROA_CODE));

        Double portfolioPe = health.current().pe();
        Double portfolioPb = health.current().pb();
        Double portfolioPs = health.current().ps();

        PortfolioHealthOutput.HistoryPoint latestHistory = health.history().stream()
                .max(Comparator.comparingInt(PortfolioHealthOutput.HistoryPoint::year)
                        .thenComparingInt(PortfolioHealthOutput.HistoryPoint::quarter))
                .orElse(null);

        Double portfolioRoe = latestHistory != null ? latestHistory.roe() : null;
        Double portfolioRoa = latestHistory != null ? latestHistory.roa() : null;

        return new PortfolioMarketBenchmarkOutput(
                code,
                compare(portfolioPe, benchmark.get(PE_CODE)),
                compare(portfolioPb, benchmark.get(PB_CODE)),
                compare(portfolioPs, benchmark.get(PS_CODE)),
                compare(portfolioRoe, benchmark.get(ROE_CODE)),
                compare(portfolioRoa, benchmark.get(ROA_CODE))
        );
    }

    private PortfolioMarketBenchmarkOutput.MetricComparisonOutput compare(Double portfolio, Double benchmark) {
        Double deltaPct = null;
        if (portfolio != null && benchmark != null && benchmark != 0) {
            deltaPct = ((portfolio - benchmark) / Math.abs(benchmark)) * 100.0;
        }
        return new PortfolioMarketBenchmarkOutput.MetricComparisonOutput(portfolio, benchmark, deltaPct);
    }
}
