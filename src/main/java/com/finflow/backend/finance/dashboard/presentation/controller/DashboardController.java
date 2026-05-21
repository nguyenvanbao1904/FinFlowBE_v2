package com.finflow.backend.finance.dashboard.presentation.controller;

import com.finflow.backend.common.versioning.ApiVersion;
import com.finflow.backend.finance.dashboard.application.dto.HomeInsightSnapshot;
import com.finflow.backend.finance.dashboard.application.port.in.GenerateHomeInsightPort;
import com.finflow.backend.finance.dashboard.presentation.request.HomeInsightRequest;
import com.finflow.backend.finance.dashboard.presentation.response.HomeInsightResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@ApiVersion("1")
@Tag(name = "Dashboard", description = "Dashboard AI APIs")
public class DashboardController {

    private final GenerateHomeInsightPort generateHomeInsightPort;

    @Operation(summary = "Generate a short AI insight for the home dashboard")
    @PostMapping("/home-insight")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<HomeInsightResponse> homeInsight(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody HomeInsightRequest request
    ) {
        var output = generateHomeInsightPort.execute(toSnapshot(jwt.getSubject(), request));
        return ResponseEntity.ok(HomeInsightResponse.builder()
                .title(output.title())
                .message(output.message())
                .warnings(output.warnings())
                .cached(output.cached())
                .build());
    }

    private HomeInsightSnapshot toSnapshot(String userId, HomeInsightRequest request) {
        return new HomeInsightSnapshot(
                userId,
                request.getLocale(),
                request.getTimezone(),
                request.getCurrency(),
                request.getNetWorth(),
                request.getLiquidAssets(),
                request.getDebtTotal(),
                request.getInvestmentAssets(),
                request.getTotalBalance(),
                request.getTotalIncome(),
                request.getTotalExpense(),
                request.getBudgetTargetTotal(),
                request.getBudgetSpentTotal(),
                request.getPortfolioCount(),
                request.getPortfolioCashTotal(),
                request.getPrimaryPortfolioName(),
                request.getInvestmentTotalValue()
        );
    }
}
