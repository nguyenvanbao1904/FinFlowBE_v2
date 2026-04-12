package com.finflow.backend.finance.transaction.presentation.controller;

import com.finflow.backend.finance.transaction.application.command.AddTransactionCommand;
import com.finflow.backend.finance.transaction.application.port.in.GetInternalTransactionUserContextPort;
import com.finflow.backend.finance.transaction.application.port.in.GetPersonalFinanceReportPort;
import com.finflow.backend.finance.transaction.application.port.in.InternalAddTransactionPort;
import com.finflow.backend.finance.transaction.presentation.request.AddTransactionRequest;
import com.finflow.backend.finance.transaction.presentation.response.TransactionResponse;
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

    @GetMapping("/finance-report")
    public ResponseEntity<Map<String, Object>> getPersonalFinanceReport(
            @RequestParam String userId) {
        Map<String, Object> report = getPersonalFinanceReportPort.execute(userId);
        return ResponseEntity.ok(report);
    }

    /**
     * Returns available categories and accounts for a user.
     * The AI agent needs this to map user intent (e.g. "ăn sáng")
     * to the correct categoryId and accountId before creating a transaction.
     */
    @GetMapping("/user-context")
    public ResponseEntity<Map<String, Object>> getUserContext(
            @RequestParam String userId) {
        return ResponseEntity.ok(getInternalTransactionUserContextPort.execute(userId));
    }

    /**
     * Creates a transaction on behalf of a user (called by AI agent after user confirmation).
     * Exceptions propagate to {@code GlobalExceptionHandler} for consistent error shape.
     */
    @PostMapping("/add-transaction")
    public ResponseEntity<Map<String, Object>> addTransaction(
            @RequestParam String userId,
            @RequestBody AddTransactionRequest request) {
        AddTransactionCommand command = new AddTransactionCommand(
                userId,
                request.getAmount(),
                request.getType(),
                request.getCategoryId(),
                request.getAccountId(),
                request.getNote(),
                request.getTransactionDate()
        );
        TransactionResponse response = internalAddTransactionPort.execute(command);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "OK");
        result.put("transactionId", response.getId());
        result.put("amount", response.getAmount());
        result.put("type", response.getType().name());
        result.put("categoryName", response.getCategory().getName());
        result.put("note", response.getNote());
        result.put("transactionDate", response.getTransactionDate());
        result.put("message", "Giao dịch đã được tạo thành công.");
        return ResponseEntity.ok(result);
    }
}
