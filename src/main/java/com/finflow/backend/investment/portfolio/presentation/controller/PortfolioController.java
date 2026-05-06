package com.finflow.backend.investment.portfolio.presentation.controller;

import com.finflow.backend.common.versioning.ApiVersion;
import com.finflow.backend.investment.portfolio.application.command.CreatePortfolioCommand;
import com.finflow.backend.investment.portfolio.application.command.CreatePortfolioAssetCommand;
import com.finflow.backend.investment.portfolio.application.command.CreateTradeTransactionCommand;
import com.finflow.backend.investment.portfolio.application.command.DeletePortfolioCommand;
import com.finflow.backend.investment.portfolio.application.command.ImportPortfolioSnapshotCommand;
import com.finflow.backend.investment.portfolio.application.command.UpdatePortfolioCommand;
import com.finflow.backend.investment.portfolio.application.port.in.CreateTradeTransactionPort;
import com.finflow.backend.investment.portfolio.application.port.in.DeletePortfolioPort;
import com.finflow.backend.investment.portfolio.application.port.in.GetMonthlyNetBuyPort;
import com.finflow.backend.investment.portfolio.application.port.in.GetPortfolioHealthPort;
import com.finflow.backend.investment.portfolio.application.port.in.GetPortfolioVsMarketPort;
import com.finflow.backend.investment.portfolio.application.port.in.GetTradeTransactionsPort;
import com.finflow.backend.investment.portfolio.application.port.in.ImportPortfolioSnapshotPort;
import com.finflow.backend.investment.portfolio.application.port.in.UpdatePortfolioPort;

import com.finflow.backend.investment.portfolio.application.port.in.CreatePortfolioAssetPort;
import com.finflow.backend.investment.portfolio.application.port.in.GetPortfolioAssetsPort;
import com.finflow.backend.investment.portfolio.application.port.in.CreatePortfolioPort;
import com.finflow.backend.investment.portfolio.application.port.in.GetPortfoliosPort;
import com.finflow.backend.investment.portfolio.application.query.GetPortfolioAssetsQuery;
import com.finflow.backend.investment.portfolio.application.query.GetPortfolioHealthQuery;
import com.finflow.backend.investment.portfolio.application.query.GetPortfolioVsMarketQuery;
import com.finflow.backend.investment.portfolio.application.query.GetPortfoliosQuery;
import com.finflow.backend.investment.portfolio.application.query.GetTradeTransactionsQuery;
import com.finflow.backend.investment.portfolio.presentation.mapper.PortfolioPresentationMapper;
import com.finflow.backend.investment.portfolio.presentation.request.CreatePortfolioRequest;
import com.finflow.backend.investment.portfolio.presentation.request.CreatePortfolioAssetRequest;
import com.finflow.backend.investment.portfolio.presentation.request.CreateTradeTransactionRequest;
import com.finflow.backend.investment.portfolio.presentation.request.ImportPortfolioSnapshotRequest;
import com.finflow.backend.investment.portfolio.presentation.request.UpdatePortfolioRequest;
import com.finflow.backend.investment.portfolio.presentation.response.PortfolioAssetResponse;
import com.finflow.backend.investment.portfolio.presentation.response.PortfolioHealthResponse;
import com.finflow.backend.investment.portfolio.presentation.response.PortfolioMarketBenchmarkResponse;
import com.finflow.backend.investment.portfolio.presentation.response.PortfolioResponse;
import com.finflow.backend.investment.portfolio.presentation.response.TradeTransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/investments/portfolios")
@ApiVersion("1")
@RequiredArgsConstructor
@Tag(name = "Portfolio", description = "Investment portfolio management APIs")
public class PortfolioController {

    private final CreatePortfolioPort createPortfolioUseCase;
    private final UpdatePortfolioPort updatePortfolioUseCase;
    private final DeletePortfolioPort deletePortfolioUseCase;
    private final GetPortfoliosPort getPortfoliosUseCase;
    private final GetPortfolioAssetsPort getPortfolioAssetsUseCase;
    private final CreatePortfolioAssetPort createPortfolioAssetUseCase;
    private final CreateTradeTransactionPort createTradeTransactionUseCase;
    private final ImportPortfolioSnapshotPort importPortfolioSnapshotUseCase;
    private final GetPortfolioHealthPort getPortfolioHealthUseCase;
    private final GetPortfolioVsMarketPort getPortfolioVsMarketUseCase;
    private final GetTradeTransactionsPort getTradeTransactionsUseCase;
    private final GetMonthlyNetBuyPort getMonthlyNetBuyUseCase;
    private final PortfolioPresentationMapper mapper;

    @Operation(summary = "Get all portfolios of current user")
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<PortfolioResponse>> getPortfolios(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(mapper.toPortfolioResponses(
                getPortfoliosUseCase.execute(new GetPortfoliosQuery(userId))));
    }

    @Operation(summary = "Get all assets of a portfolio (current user)")
    @GetMapping("/{portfolioId}/assets")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<PortfolioAssetResponse>> getPortfolioAssets(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID portfolioId
    ) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(mapper.toAssetResponses(
                getPortfolioAssetsUseCase.execute(new GetPortfolioAssetsQuery(userId, portfolioId))));
    }

    @Operation(summary = "Add an asset to a portfolio (current user)")
    @PostMapping("/{portfolioId}/assets")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<PortfolioAssetResponse> createPortfolioAsset(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID portfolioId,
            @Valid @RequestBody CreatePortfolioAssetRequest request
    ) {
        String userId = jwt.getSubject();
        var output = createPortfolioAssetUseCase.execute(
            new CreatePortfolioAssetCommand(userId, portfolioId, request.getSymbol(),
                request.getQuantity(), request.getAveragePrice())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(output));
    }

    @Operation(summary = "Create a new empty portfolio (cashBalance=0)")
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<PortfolioResponse> createPortfolio(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreatePortfolioRequest request
    ) {
        String userId = jwt.getSubject();
        var output = createPortfolioUseCase.execute(
            new CreatePortfolioCommand(userId, request.getName())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(output));
    }

    @Operation(summary = "Get paginated trade transaction history of a portfolio")
    @GetMapping("/{portfolioId}/transactions")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<TradeTransactionResponse>> getTradeTransactions(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID portfolioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        String userId = jwt.getSubject();
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size, 50));
        var result = getTradeTransactionsUseCase.execute(
            new GetTradeTransactionsQuery(userId, portfolioId, safePage, safeSize));
        return ResponseEntity.ok(result.map(mapper::toResponse));
    }

    @Operation(summary = "Create a new trade transaction (BUY/SELL/DEPOSIT/WITHDRAW)")
    @PostMapping("/{portfolioId}/transactions")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> createTradeTransaction(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID portfolioId,
            @Valid @RequestBody CreateTradeTransactionRequest request
    ) {
        String userId = jwt.getSubject();
        CreateTradeTransactionCommand command = new CreateTradeTransactionCommand(
                userId,
                portfolioId,
                request.getTradeType(),
                request.getSymbol(),
                request.getQuantity(),
                request.getPrice(),
                request.getAmount(),
                request.getFeePercent(),
                request.getTaxPercent(),
                request.getTransactionDate()
        );
        createTradeTransactionUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Import/overwrite portfolio snapshot (cashBalance + holdings)")
    @PostMapping("/{portfolioId}/import-snapshot")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> importPortfolioSnapshot(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID portfolioId,
            @Valid @RequestBody ImportPortfolioSnapshotRequest request
    ) {
        String userId = jwt.getSubject();
        importPortfolioSnapshotUseCase.execute(new ImportPortfolioSnapshotCommand(
            userId, portfolioId, request.getCashBalance(),
            request.getHoldings() != null ? request.getHoldings().stream()
                .map(h -> new ImportPortfolioSnapshotCommand.HoldingSnapshot(
                    h.getSymbol(), h.getTotalQuantity(), h.getAveragePrice()
                )).toList() : java.util.List.of()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Get portfolio health metrics (P/E, P/B, P/S, ROE, ROA history + current close price snapshot)")
    @GetMapping("/{portfolioId}/health")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<PortfolioHealthResponse> getPortfolioHealth(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID portfolioId,
            @RequestParam(defaultValue = "12") int quarters
    ) {
        String userId = jwt.getSubject();
        int safeQuarters = Math.max(1, Math.min(quarters, 40));
        var result = getPortfolioHealthUseCase.execute(new GetPortfolioHealthQuery(userId, portfolioId, safeQuarters));
        return ResponseEntity.ok(mapper.toHealthResponse(result));
    }

    @Operation(summary = "Compare portfolio metrics against market benchmark (default VNINDEX)")
    @GetMapping("/{portfolioId}/benchmark")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<PortfolioMarketBenchmarkResponse> getPortfolioBenchmark(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID portfolioId,
            @RequestParam(defaultValue = "VNINDEX") String code
    ) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(mapper.toBenchmarkResponse(
                getPortfolioVsMarketUseCase.execute(new GetPortfolioVsMarketQuery(userId, portfolioId, code))));
    }

    @Operation(summary = "Rename a portfolio")
    @PutMapping("/{portfolioId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<PortfolioResponse> updatePortfolio(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID portfolioId,
            @Valid @RequestBody UpdatePortfolioRequest request
    ) {
        String userId = jwt.getSubject();
        var output = updatePortfolioUseCase.execute(
                new UpdatePortfolioCommand(userId, portfolioId, request.getName()));
        return ResponseEntity.ok(mapper.toResponse(output));
    }

    @Operation(summary = "Delete a portfolio and all its assets and trade transactions")
    @DeleteMapping("/{portfolioId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deletePortfolio(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID portfolioId
    ) {
        String userId = jwt.getSubject();
        deletePortfolioUseCase.execute(new DeletePortfolioCommand(userId, portfolioId));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Total BUY trade amount for current user in a given month (default: current month)")
    @GetMapping("/monthly-net-buy")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, BigDecimal>> getMonthlyNetBuy(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String month
    ) {
        String userId = jwt.getSubject();
        YearMonth yearMonth = (month != null && !month.isBlank())
                ? YearMonth.parse(month)
                : YearMonth.now();
        BigDecimal amount = getMonthlyNetBuyUseCase.execute(userId, yearMonth);
        return ResponseEntity.ok(Map.of("monthlyNetBuy", amount));
    }

}
