package com.finflow.backend.finance.transaction.presentation.controller;

import com.finflow.backend.finance.transaction.application.command.AddTransactionCommand;
import com.finflow.backend.finance.transaction.application.port.in.GetInternalTransactionUserContextPort;
import com.finflow.backend.finance.transaction.application.port.in.GetPersonalFinanceReportPort;
import com.finflow.backend.finance.transaction.application.port.in.InternalAddTransactionPort;
import com.finflow.backend.finance.transaction.application.query.GetInternalTransactionUserContextQuery;
import com.finflow.backend.finance.transaction.application.query.GetPersonalFinanceReportQuery;
import com.finflow.backend.finance.transaction.presentation.mapper.TransactionPresentationMapper;
import com.finflow.backend.finance.transaction.presentation.request.AddTransactionRequest;
import com.finflow.backend.finance.transaction.presentation.response.InternalFinanceReportResponse;
import com.finflow.backend.finance.transaction.presentation.response.InternalUserContextResponse;
import com.finflow.backend.finance.transaction.presentation.response.TransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Internal API for AI agent — no JWT needed, uses X-Internal-Api-Key.
 * Provides personal finance data + transaction mutation capabilities
 * so the chat agent can both read and write transaction data.
 */
@RestController
@RequestMapping("/api/internal/transaction")
@RequiredArgsConstructor
public class InternalTransactionController {

    private final GetPersonalFinanceReportPort getPersonalFinanceReportPort;
    private final InternalAddTransactionPort internalAddTransactionPort;
    private final GetInternalTransactionUserContextPort getInternalTransactionUserContextPort;
    private final TransactionPresentationMapper mapper;

    @Operation(summary = "Get personal finance report for user (internal)")
    @GetMapping("/finance-report")
    public ResponseEntity<InternalFinanceReportResponse> getPersonalFinanceReport(
            @RequestParam String userId) {
        var output = getPersonalFinanceReportPort.execute(new GetPersonalFinanceReportQuery(userId));
        return ResponseEntity.ok(mapper.toInternalFinanceReportResponse(output));
    }

    /**
     * Returns available categories and accounts for a user.
     * The AI agent needs this to map user intent (e.g. "ăn sáng")
     * to the correct categoryId and accountId before creating a transaction.
     */
    @Operation(summary = "Get user categories and accounts context (internal)")
    @GetMapping("/user-context")
    public ResponseEntity<InternalUserContextResponse> getUserContext(
            @RequestParam String userId) {
        var output = getInternalTransactionUserContextPort.execute(new GetInternalTransactionUserContextQuery(userId));
        return ResponseEntity.ok(mapper.toInternalUserContextResponse(output));
    }

    /**
     * Creates a transaction on behalf of a user (called by AI agent after user confirmation).
     * Exceptions propagate to {@code GlobalExceptionHandler} for consistent error shape.
     */
    @Operation(summary = "Create transaction on behalf of user (internal)")
    @PostMapping("/add-transaction")
    public ResponseEntity<Map<String, Object>> addTransaction(
            @RequestParam String userId,
            @Valid @RequestBody AddTransactionRequest request) {
        AddTransactionCommand command = new AddTransactionCommand(
                userId,
                request.getAmount(),
                request.getType(),
                request.getCategoryId(),
                request.getAccountId(),
                request.getNote(),
                request.getTransactionDate()
        );
        TransactionResponse response = mapper.toResponse(internalAddTransactionPort.execute(command));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "OK");
        result.put("transactionId", response.getId());
        result.put("amount", response.getAmount());
        result.put("type", response.getType());
        result.put("categoryName", response.getCategory() == null ? null : response.getCategory().getName());
        result.put("note", response.getNote());
        result.put("transactionDate", response.getTransactionDate());
        result.put("message", "Giao dịch đã được tạo thành công.");
        return ResponseEntity.ok(result);
    }
}
