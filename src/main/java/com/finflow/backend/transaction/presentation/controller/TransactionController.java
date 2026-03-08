package com.finflow.backend.transaction.presentation.controller;

import com.finflow.backend.transaction.application.usecase.AddTransactionUseCase;
import com.finflow.backend.transaction.application.usecase.AnalyzeTransactionUseCase;
import com.finflow.backend.transaction.application.usecase.DeleteTransactionUseCase;
import com.finflow.backend.transaction.application.usecase.GetCategoriesUseCase;
import com.finflow.backend.transaction.application.usecase.GetTransactionChartUseCase;
import com.finflow.backend.transaction.application.usecase.GetTransactionSummaryUseCase;
import com.finflow.backend.transaction.application.usecase.GetTransactionsUseCase;
import com.finflow.backend.transaction.application.usecase.UpdateTransactionUseCase;
import com.finflow.backend.transaction.presentation.request.AddTransactionRequest;
import com.finflow.backend.transaction.presentation.request.AnalyzeTransactionRequest;
import com.finflow.backend.transaction.presentation.request.UpdateTransactionRequest;
import com.finflow.backend.transaction.presentation.response.AnalyzeTransactionResponse;
import com.finflow.backend.transaction.presentation.response.CategoryResponse;
import com.finflow.backend.transaction.presentation.response.TransactionChartResponse;
import com.finflow.backend.transaction.presentation.response.TransactionResponse;
import com.finflow.backend.transaction.presentation.response.TransactionSummaryResponse;
import com.finflow.backend.common.versioning.ApiVersion;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@ApiVersion("1")
public class TransactionController {

    private final AddTransactionUseCase addTransactionUseCase;
    private final GetTransactionsUseCase getTransactionsUseCase;
    private final GetTransactionSummaryUseCase getTransactionSummaryUseCase;
    private final GetCategoriesUseCase getCategoriesUseCase;
    private final AnalyzeTransactionUseCase analyzeTransactionUseCase;
    private final GetTransactionChartUseCase getTransactionChartUseCase;
    private final UpdateTransactionUseCase updateTransactionUseCase;
    private final DeleteTransactionUseCase deleteTransactionUseCase;

    @PostMapping
    public ResponseEntity<TransactionResponse> addTransaction(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AddTransactionRequest request) {
        String userId = jwt.getSubject();
        TransactionResponse response = addTransactionUseCase.execute(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/analyze")
    public ResponseEntity<AnalyzeTransactionResponse> analyzeTransaction(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AnalyzeTransactionRequest request) {
        String userId = jwt.getSubject();
        AnalyzeTransactionResponse response = analyzeTransactionUseCase.execute(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String keyword) {
        String userId = jwt.getSubject();
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
        LocalDate end   = endDate   != null ? LocalDate.parse(endDate)   : null;
        Page<TransactionResponse> response = getTransactionsUseCase.execute(userId, page, size, start, end, keyword);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<TransactionSummaryResponse> getTransactionSummary(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        TransactionSummaryResponse response = getTransactionSummaryUseCase.execute(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/chart")
    public ResponseEntity<TransactionChartResponse> getTransactionChart(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "MONTH") String range,
            @RequestParam(required = false) String referenceDate) {
        String userId = jwt.getSubject();
        GetTransactionChartUseCase.ChartRange chartRange =
                GetTransactionChartUseCase.ChartRange.valueOf(range.toUpperCase());
        LocalDate refDate = referenceDate != null ? LocalDate.parse(referenceDate) : null;
        TransactionChartResponse response = getTransactionChartUseCase.execute(userId, chartRange, refDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getCategories(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<CategoryResponse> response = getCategoriesUseCase.execute(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTransactionRequest request) {
        String userId = jwt.getSubject();
        TransactionResponse response = updateTransactionUseCase.execute(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {
        String userId = jwt.getSubject();
        deleteTransactionUseCase.execute(userId, id);
        return ResponseEntity.noContent().build();
    }
}
