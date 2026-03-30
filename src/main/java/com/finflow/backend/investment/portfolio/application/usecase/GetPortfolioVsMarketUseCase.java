package com.finflow.backend.investment.portfolio.application.usecase;

import com.finflow.backend.investment.portfolio.infrastructure.VndirectRatiosClient;
import com.finflow.backend.investment.portfolio.presentation.response.PortfolioHealthResponse;
import com.finflow.backend.investment.portfolio.presentation.response.PortfolioMarketBenchmarkResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPortfolioVsMarketUseCase {

    private static final String PE_CODE = "81007";
    private static final String PB_CODE = "81013";
    private static final String PS_CODE = "81017";
    private static final String ROE_CODE = "82008";
    private static final String ROA_CODE = "82006";

    private final GetPortfolioHealthUseCase getPortfolioHealthUseCase;
    private final VndirectRatiosClient vndirectRatiosClient;

    @Transactional(readOnly = true)
    public PortfolioMarketBenchmarkResponse execute(String userId, UUID portfolioId, String benchmarkCode) {
        String code = benchmarkCode == null || benchmarkCode.isBlank()
                ? "VNINDEX"
                : benchmarkCode.trim().toUpperCase();

        PortfolioHealthResponse health = getPortfolioHealthUseCase.execute(userId, portfolioId, 20);
        Map<String, Double> benchmark = vndirectRatiosClient.getLatestRatios(
                code,
                List.of(PE_CODE, PB_CODE, PS_CODE, ROE_CODE, ROA_CODE)
        );

        Double portfolioPe = health.current().pe();
        Double portfolioPb = health.current().pb();
        Double portfolioPs = health.current().ps();

        // ROE/ROA currently available as quarterly history values in portfolio health.
        PortfolioHealthResponse.HistoryPoint latestHistory = health.history().stream()
                .max(Comparator.comparingInt(PortfolioHealthResponse.HistoryPoint::year)
                        .thenComparingInt(PortfolioHealthResponse.HistoryPoint::quarter))
                .orElse(null);

        Double portfolioRoe = latestHistory != null ? latestHistory.roe() : null;
        Double portfolioRoa = latestHistory != null ? latestHistory.roa() : null;

        return new PortfolioMarketBenchmarkResponse(
                code,
                compare(portfolioPe, benchmark.get(PE_CODE)),
                compare(portfolioPb, benchmark.get(PB_CODE)),
                compare(portfolioPs, benchmark.get(PS_CODE)),
                compare(portfolioRoe, benchmark.get(ROE_CODE)),
                compare(portfolioRoa, benchmark.get(ROA_CODE))
        );
    }

    private PortfolioMarketBenchmarkResponse.MetricComparison compare(Double portfolio, Double benchmark) {
        Double deltaPct = null;
        if (portfolio != null && benchmark != null && benchmark != 0) {
            deltaPct = ((portfolio - benchmark) / Math.abs(benchmark)) * 100.0;
        }
        return new PortfolioMarketBenchmarkResponse.MetricComparison(portfolio, benchmark, deltaPct);
    }
}

