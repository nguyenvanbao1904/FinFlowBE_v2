package com.finflow.backend.investment.portfolio.presentation.mapper;

import com.finflow.backend.investment.portfolio.application.dto.PortfolioAssetOutput;
import com.finflow.backend.investment.portfolio.application.dto.PortfolioHealthOutput;
import com.finflow.backend.investment.portfolio.application.dto.PortfolioMarketBenchmarkOutput;
import com.finflow.backend.investment.portfolio.application.dto.PortfolioResponseOutput;
import com.finflow.backend.investment.portfolio.presentation.response.PortfolioAssetResponse;
import com.finflow.backend.investment.portfolio.presentation.response.PortfolioHealthResponse;
import com.finflow.backend.investment.portfolio.presentation.response.PortfolioMarketBenchmarkResponse;
import com.finflow.backend.investment.portfolio.presentation.response.PortfolioResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE,
        unmappedSourcePolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface PortfolioPresentationMapper {

    PortfolioResponse toResponse(PortfolioResponseOutput output);

    PortfolioAssetResponse toResponse(PortfolioAssetOutput output);

    List<PortfolioResponse> toPortfolioResponses(List<PortfolioResponseOutput> outputs);

    List<PortfolioAssetResponse> toAssetResponses(List<PortfolioAssetOutput> outputs);

    PortfolioHealthResponse.CurrentSnapshot toCurrentSnapshot(PortfolioHealthOutput.CurrentSnapshot output);

    PortfolioHealthResponse.HistoryPoint toHistoryPoint(PortfolioHealthOutput.HistoryPoint output);

    List<PortfolioHealthResponse.HistoryPoint> toHistoryPoints(List<PortfolioHealthOutput.HistoryPoint> outputs);

    PortfolioMarketBenchmarkResponse.MetricComparison toMetricComparison(PortfolioMarketBenchmarkOutput.MetricComparisonOutput output);

    default PortfolioHealthResponse toHealthResponse(PortfolioHealthOutput output) {
        return new PortfolioHealthResponse(
                output.latestYear(),
                output.latestQuarter(),
                toCurrentSnapshot(output.current()),
                toHistoryPoints(output.history())
        );
    }

    default PortfolioMarketBenchmarkResponse toBenchmarkResponse(PortfolioMarketBenchmarkOutput output) {
        return new PortfolioMarketBenchmarkResponse(
                output.benchmarkCode(),
                toMetricComparison(output.pe()),
                toMetricComparison(output.pb()),
                toMetricComparison(output.ps()),
                toMetricComparison(output.roe()),
                toMetricComparison(output.roa())
        );
    }
}
