package com.finflow.backend.finance.transaction.presentation.controller;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.finance.common.enums.TransactionChartRange;
import com.finflow.backend.finance.transaction.application.query.AnalyzeTransactionQuery;
import com.finflow.backend.finance.transaction.application.query.GetTransactionAnalyticsInsightsQuery;
import com.finflow.backend.finance.transaction.application.query.GetTransactionChartQuery;
import com.finflow.backend.finance.transaction.application.query.GetTransactionSummaryQuery;
import com.finflow.backend.finance.transaction.application.port.in.AnalyzeTransactionPort;
import com.finflow.backend.finance.transaction.application.port.in.GetTransactionAnalyticsInsightsPort;
import com.finflow.backend.finance.transaction.application.port.in.GetTransactionChartPort;
import com.finflow.backend.finance.transaction.application.port.in.GetTransactionSummaryPort;
import com.finflow.backend.finance.transaction.presentation.mapper.TransactionPresentationMapper;
import com.finflow.backend.finance.transaction.presentation.request.AnalyzeTransactionRequest;
import com.finflow.backend.finance.transaction.presentation.response.AnalyzeTransactionResponse;
import com.finflow.backend.finance.transaction.presentation.response.TransactionAnalyticsInsightsResponse;
import com.finflow.backend.finance.transaction.presentation.response.TransactionChartResponse;
import com.finflow.backend.finance.transaction.presentation.response.TransactionSummaryResponse;
import com.finflow.backend.finance.transaction.exception.TransactionErrorCode;
import com.finflow.backend.common.versioning.ApiVersion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

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
    private final TransactionPresentationMapper mapper;

    @Operation(summary = "Get transaction summary (income/expense totals)")
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<TransactionSummaryResponse> getTransactionSummary(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(mapper.toResponse(
                getTransactionSummaryPort.execute(new GetTransactionSummaryQuery(userId))));
    }

    @Operation(summary = "Get transaction summary for a specific month (default: current month)")
    @GetMapping("/summary/monthly")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<TransactionSummaryResponse> getMonthlySummary(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String month) {
        String userId = jwt.getSubject();
        YearMonth ym = month != null ? YearMonth.parse(month) : YearMonth.now();
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        return ResponseEntity.ok(mapper.toResponse(
                getTransactionSummaryPort.execute(new GetTransactionSummaryQuery(userId, start, end))));
    }

    @Operation(summary = "Get transaction chart data by range (MONTH, etc.)")
    @GetMapping("/chart")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<TransactionChartResponse> getTransactionChart(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "MONTH") String range,
            @RequestParam(required = false) String referenceDate) {
        String userId = jwt.getSubject();
        TransactionChartRange chartRange;
        try {
            chartRange = TransactionChartRange.valueOf(range.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(TransactionErrorCode.INVALID_CHART_RANGE);
        }
        LocalDate refDate = null;
        if (referenceDate != null) {
            try {
                refDate = LocalDate.parse(referenceDate);
            } catch (DateTimeParseException e) {
                throw new AppException(TransactionErrorCode.INVALID_TRANSACTION_DATE);
            }
        }
        return ResponseEntity.ok(mapper.toResponse(
                getTransactionChartPort.execute(new GetTransactionChartQuery(userId, chartRange, refDate))));
    }

    @Operation(summary = "Analyze transaction text and suggest category")
    @PostMapping("/analyze")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<AnalyzeTransactionResponse> analyzeTransaction(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AnalyzeTransactionRequest request) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(mapper.toResponse(
                analyzeTransactionPort.execute(new AnalyzeTransactionQuery(userId, request.getText()))));
    }

    @Operation(summary = "Get AI analytics insights (read-only)")
    @GetMapping("/analytics-insights")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<TransactionAnalyticsInsightsResponse> getAnalyticsInsights(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(mapper.toResponse(
                getTransactionAnalyticsInsightsPort.execute(new GetTransactionAnalyticsInsightsQuery(userId))));
    }
}
