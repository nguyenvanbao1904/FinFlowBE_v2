package com.finflow.backend.finance.transaction.presentation.controller;

import com.finflow.backend.finance.transaction.application.TransactionChartRange;
import com.finflow.backend.finance.transaction.application.command.AnalyzeTransactionCommand;
import com.finflow.backend.finance.transaction.application.port.in.AnalyzeTransactionPort;
import com.finflow.backend.finance.transaction.application.port.in.GetTransactionAnalyticsInsightsPort;
import com.finflow.backend.finance.transaction.application.port.in.GetTransactionChartPort;
import com.finflow.backend.finance.transaction.application.port.in.GetTransactionSummaryPort;
import com.finflow.backend.finance.transaction.presentation.request.AnalyzeTransactionRequest;
import com.finflow.backend.finance.transaction.presentation.response.AnalyzeTransactionResponse;
import com.finflow.backend.finance.transaction.presentation.response.TransactionAnalyticsInsightResponse;
import com.finflow.backend.finance.transaction.presentation.response.TransactionAnalyticsInsightsResponse;
import com.finflow.backend.finance.transaction.presentation.response.TransactionChartResponse;
import com.finflow.backend.finance.transaction.presentation.response.TransactionSummaryResponse;
import com.finflow.backend.common.versioning.ApiVersion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@ApiVersion("1")
@Tag(name = "Transaction Analytics", description = "Transaction analytics, charts, and AI insights APIs")
public class TransactionAnalyticsController {

    private final GetTransactionSummaryPort getTransactionSummaryPort;
    private final GetTransactionChartPort getTransactionChartPort;
    private final AnalyzeTransactionPort analyzeTransactionPort;
    private final GetTransactionAnalyticsInsightsPort getTransactionAnalyticsInsightsPort;

    @Operation(summary = "Get transaction summary (income/expense totals)")
    @GetMapping("/summary")
    public ResponseEntity<TransactionSummaryResponse> getTransactionSummary(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        TransactionSummaryResponse response = getTransactionSummaryPort.execute(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get transaction chart data by range (MONTH, etc.)")
    @GetMapping("/chart")
    public ResponseEntity<TransactionChartResponse> getTransactionChart(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "MONTH") String range,
            @RequestParam(required = false) String referenceDate) {
        String userId = jwt.getSubject();
        TransactionChartRange chartRange =
                TransactionChartRange.valueOf(range.toUpperCase());
        LocalDate refDate = referenceDate != null ? LocalDate.parse(referenceDate) : null;
        TransactionChartResponse response = getTransactionChartPort.execute(userId, chartRange, refDate);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Analyze transaction text and suggest category")
    @PostMapping("/analyze")
    public ResponseEntity<AnalyzeTransactionResponse> analyzeTransaction(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AnalyzeTransactionRequest request) {
        String userId = jwt.getSubject();
        AnalyzeTransactionResponse response = analyzeTransactionPort.execute(
                userId, new AnalyzeTransactionCommand(request.getText()));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get AI analytics insights (read-only)")
    @GetMapping("/analytics-insights")
    public ResponseEntity<TransactionAnalyticsInsightsResponse> getAnalyticsInsights(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        var result = getTransactionAnalyticsInsightsPort.execute(userId);
        var insights = result.insights().stream()
                .map(i -> TransactionAnalyticsInsightResponse.builder()
                        .id(i.id()).type(i.type()).title(i.title()).message(i.message()).confidence(i.confidence())
                        .build())
                .toList();
        return ResponseEntity.ok(TransactionAnalyticsInsightsResponse.builder()
                .insights(insights)
                .cached(result.cached())
                .build());
    }
}
