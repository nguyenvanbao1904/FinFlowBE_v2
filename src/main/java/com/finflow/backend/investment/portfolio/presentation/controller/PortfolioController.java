package com.finflow.backend.investment.portfolio.presentation.controller;

import com.finflow.backend.common.versioning.ApiVersion;
import com.finflow.backend.investment.portfolio.application.usecase.CreateTradeTransactionUseCase;
import com.finflow.backend.investment.portfolio.application.usecase.GetPortfolioHealthUseCase;
import com.finflow.backend.investment.portfolio.application.usecase.GetPortfolioPerformanceUseCase;
import com.finflow.backend.investment.portfolio.application.usecase.GetPortfolioVsMarketUseCase;
import com.finflow.backend.investment.portfolio.application.usecase.ImportPortfolioSnapshotUseCase;
        
import com.finflow.backend.investment.portfolio.application.usecase.CreatePortfolioAssetUseCase;
import com.finflow.backend.investment.portfolio.application.usecase.GetPortfolioAssetsUseCase;
import com.finflow.backend.investment.portfolio.application.usecase.CreatePortfolioUseCase;
import com.finflow.backend.investment.portfolio.application.usecase.GetPortfoliosUseCase;
import com.finflow.backend.investment.portfolio.presentation.request.CreatePortfolioRequest;
import com.finflow.backend.investment.portfolio.presentation.request.CreatePortfolioAssetRequest;
import com.finflow.backend.investment.portfolio.presentation.request.CreateTradeTransactionRequest;
import com.finflow.backend.investment.portfolio.presentation.request.ImportPortfolioSnapshotRequest;
import com.finflow.backend.investment.portfolio.presentation.response.PortfolioAssetResponse;
import com.finflow.backend.investment.portfolio.presentation.response.PortfolioHealthResponse;
import com.finflow.backend.investment.portfolio.presentation.response.PortfolioMarketBenchmarkResponse;
import com.finflow.backend.investment.portfolio.presentation.response.PortfolioPerformanceResponse;
import com.finflow.backend.investment.portfolio.presentation.response.PortfolioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/investments/portfolios")
@ApiVersion("1")
@RequiredArgsConstructor
@Tag(name = "Portfolio", description = "Investment portfolio management APIs")
public class PortfolioController {

    private final CreatePortfolioUseCase createPortfolioUseCase;
    private final GetPortfoliosUseCase getPortfoliosUseCase;
    private final GetPortfolioAssetsUseCase getPortfolioAssetsUseCase;
    private final CreatePortfolioAssetUseCase createPortfolioAssetUseCase;
    private final CreateTradeTransactionUseCase createTradeTransactionUseCase;
    private final ImportPortfolioSnapshotUseCase importPortfolioSnapshotUseCase;
    private final GetPortfolioHealthUseCase getPortfolioHealthUseCase;
    private final GetPortfolioVsMarketUseCase getPortfolioVsMarketUseCase;
    private final GetPortfolioPerformanceUseCase getPortfolioPerformanceUseCase;

    @Operation(summary = "Get all portfolios of current user")
    @GetMapping
    public ResponseEntity<List<PortfolioResponse>> getPortfolios(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(getPortfoliosUseCase.execute(userId));
    }

    @Operation(summary = "Get all assets of a portfolio (current user)")
    @GetMapping("/{portfolioId}/assets")
    public ResponseEntity<List<PortfolioAssetResponse>> getPortfolioAssets(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID portfolioId
    ) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(getPortfolioAssetsUseCase.execute(userId, portfolioId));
    }

    @Operation(summary = "Add an asset to a portfolio (current user)")
    @PostMapping("/{portfolioId}/assets")
    public ResponseEntity<PortfolioAssetResponse> createPortfolioAsset(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID portfolioId,
            @Valid @RequestBody CreatePortfolioAssetRequest request
    ) {
        String userId = jwt.getSubject();
        PortfolioAssetResponse response = createPortfolioAssetUseCase.execute(userId, portfolioId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Create a new empty portfolio (cashBalance=0)")
    @PostMapping
    public ResponseEntity<PortfolioResponse> createPortfolio(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreatePortfolioRequest request
    ) {
        String userId = jwt.getSubject();
        PortfolioResponse response = createPortfolioUseCase.execute(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Create a new trade transaction (BUY/SELL/DEPOSIT/WITHDRAW)")
    @PostMapping("/{portfolioId}/transactions")
    public ResponseEntity<Void> createTradeTransaction(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID portfolioId,
            @Valid @RequestBody CreateTradeTransactionRequest request
    ) {
        String userId = jwt.getSubject();
        createTradeTransactionUseCase.execute(userId, portfolioId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Import/overwrite portfolio snapshot (cashBalance + holdings)")
    @PostMapping("/{portfolioId}/import-snapshot")
    public ResponseEntity<Void> importPortfolioSnapshot(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID portfolioId,
            @Valid @RequestBody ImportPortfolioSnapshotRequest request
    ) {
        String userId = jwt.getSubject();
        importPortfolioSnapshotUseCase.execute(userId, portfolioId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Get portfolio health metrics (P/E, P/B, P/S, ROE, ROA history + current close price snapshot)")
    @GetMapping("/{portfolioId}/health")
    public ResponseEntity<PortfolioHealthResponse> getPortfolioHealth(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID portfolioId,
            @RequestParam(defaultValue = "12") int quarters
    ) {
        String userId = jwt.getSubject();
        int safeQuarters = Math.max(1, Math.min(quarters, 40));
        return ResponseEntity.ok(getPortfolioHealthUseCase.execute(userId, portfolioId, safeQuarters));
    }

    @Operation(summary = "Compare portfolio metrics against market benchmark (default VNINDEX)")
    @GetMapping("/{portfolioId}/benchmark")
    public ResponseEntity<PortfolioMarketBenchmarkResponse> getPortfolioBenchmark(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID portfolioId,
            @RequestParam(defaultValue = "VNINDEX") String code
    ) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(getPortfolioVsMarketUseCase.execute(userId, portfolioId, code));
    }

    @Operation(summary = "NAV vs VNINDEX time series (snapshot DB) for chart; returnPct neo 0% tại điểm đầu chuỗi")
    @GetMapping("/{portfolioId}/performance")
    public ResponseEntity<PortfolioPerformanceResponse> getPortfolioPerformance(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID portfolioId,
            @RequestParam(defaultValue = "1Y") String range,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        String userId = jwt.getSubject();
        GetPortfolioPerformanceUseCase.PerformanceRange r = GetPortfolioPerformanceUseCase.PerformanceRange.fromParam(range);
        return ResponseEntity.ok(getPortfolioPerformanceUseCase.execute(userId, portfolioId, r, startDate, endDate));
    }
}

