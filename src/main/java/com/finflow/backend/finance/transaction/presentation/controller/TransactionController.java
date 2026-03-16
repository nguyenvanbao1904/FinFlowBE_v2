package com.finflow.backend.finance.transaction.presentation.controller;

import com.finflow.backend.finance.transaction.application.usecase.AddTransactionUseCase;
import com.finflow.backend.finance.transaction.application.usecase.AnalyzeTransactionUseCase;
import com.finflow.backend.finance.transaction.application.usecase.DeleteTransactionUseCase;
import com.finflow.backend.finance.transaction.application.usecase.CreateCategoryUseCase;
import com.finflow.backend.finance.transaction.application.usecase.DeleteCategoryUseCase;
import com.finflow.backend.finance.transaction.application.usecase.GetCategoriesUseCase;
import com.finflow.backend.finance.transaction.application.usecase.GetTransactionChartUseCase;
import com.finflow.backend.finance.transaction.application.usecase.UpdateCategoryUseCase;
import com.finflow.backend.finance.transaction.application.usecase.GetTransactionSummaryUseCase;
import com.finflow.backend.finance.transaction.application.usecase.GetTransactionsUseCase;
import com.finflow.backend.finance.transaction.application.usecase.UpdateTransactionUseCase;
import com.finflow.backend.finance.transaction.presentation.request.AddTransactionRequest;
import com.finflow.backend.finance.transaction.presentation.request.AnalyzeTransactionRequest;
import com.finflow.backend.finance.transaction.presentation.request.CreateCategoryRequest;
import com.finflow.backend.finance.transaction.presentation.request.UpdateCategoryRequest;
import com.finflow.backend.finance.transaction.presentation.request.UpdateTransactionRequest;
import com.finflow.backend.finance.transaction.presentation.response.AnalyzeTransactionResponse;
import com.finflow.backend.finance.transaction.presentation.response.CategoryResponse;
import com.finflow.backend.finance.transaction.presentation.response.TransactionChartResponse;
import com.finflow.backend.finance.transaction.presentation.response.TransactionResponse;
import com.finflow.backend.finance.transaction.presentation.response.TransactionSummaryResponse;
import com.finflow.backend.common.versioning.ApiVersion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Transaction", description = "Transaction and category management APIs")
public class TransactionController {

    private final AddTransactionUseCase addTransactionUseCase;
    private final GetTransactionsUseCase getTransactionsUseCase;
    private final GetTransactionSummaryUseCase getTransactionSummaryUseCase;
    private final GetCategoriesUseCase getCategoriesUseCase;
    private final CreateCategoryUseCase createCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;
    private final AnalyzeTransactionUseCase analyzeTransactionUseCase;
    private final GetTransactionChartUseCase getTransactionChartUseCase;
    private final UpdateTransactionUseCase updateTransactionUseCase;
    private final DeleteTransactionUseCase deleteTransactionUseCase;

    @Operation(summary = "Create a new transaction")
    @PostMapping
    public ResponseEntity<TransactionResponse> addTransaction(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AddTransactionRequest request) {
        String userId = jwt.getSubject();
        TransactionResponse response = addTransactionUseCase.execute(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Analyze transaction text and suggest category")
    @PostMapping("/analyze")
    public ResponseEntity<AnalyzeTransactionResponse> analyzeTransaction(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AnalyzeTransactionRequest request) {
        String userId = jwt.getSubject();
        AnalyzeTransactionResponse response = analyzeTransactionUseCase.execute(userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get paginated transactions with optional date range and keyword")
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

    @Operation(summary = "Get transaction summary (income/expense totals)")
    @GetMapping("/summary")
    public ResponseEntity<TransactionSummaryResponse> getTransactionSummary(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        TransactionSummaryResponse response = getTransactionSummaryUseCase.execute(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get transaction chart data by range (MONTH, etc.)")
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

    @Operation(summary = "Get all categories of current user (including system categories)")
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getCategories(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<CategoryResponse> response = getCategoriesUseCase.execute(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create a new category")
    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateCategoryRequest request) {
        String userId = jwt.getSubject();
        CategoryResponse response = createCategoryUseCase.execute(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update a category (own categories only)")
    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        String userId = jwt.getSubject();
        CategoryResponse response = updateCategoryUseCase.execute(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete a category (own categories only, not in use)")
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {
        String userId = jwt.getSubject();
        deleteCategoryUseCase.execute(userId, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update a transaction")
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTransactionRequest request) {
        String userId = jwt.getSubject();
        TransactionResponse response = updateTransactionUseCase.execute(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete a transaction")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {
        String userId = jwt.getSubject();
        deleteTransactionUseCase.execute(userId, id);
        return ResponseEntity.noContent().build();
    }
}
