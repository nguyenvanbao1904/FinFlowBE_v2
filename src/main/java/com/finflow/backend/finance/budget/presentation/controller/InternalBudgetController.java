package com.finflow.backend.finance.budget.presentation.controller;

import com.finflow.backend.finance.budget.application.port.in.InternalCreateBudgetPort;
import com.finflow.backend.finance.budget.application.port.in.InternalGetBudgetsPort;
import com.finflow.backend.finance.budget.presentation.request.CreateBudgetRequest;
import com.finflow.backend.finance.budget.presentation.response.BudgetResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Internal API for AI agent — no JWT; uses {@code X-Internal-Api-Key}.
 */
@RestController
@RequestMapping("/api/internal/budget")
@RequiredArgsConstructor
public class InternalBudgetController {

    private final InternalGetBudgetsPort internalGetBudgetsUseCase;
    private final InternalCreateBudgetPort internalCreateBudgetUseCase;

    @GetMapping("/budgets")
    public ResponseEntity<List<BudgetResponse>> listBudgets(@RequestParam String userId) {
        return ResponseEntity.ok(internalGetBudgetsUseCase.execute(userId));
    }

    /**
     * Creates a budget after the user confirmed details in chat.
     */
    @PostMapping("/create-budget")
    public ResponseEntity<Map<String, Object>> createBudget(
            @RequestParam String userId,
            @Valid @RequestBody CreateBudgetRequest request) {
        BudgetResponse response = internalCreateBudgetUseCase.execute(userId, request);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "OK");
        result.put("budgetId", response.getId());
        result.put("categoryName", response.getCategory().getName());
        result.put("targetAmount", response.getTargetAmount());
        result.put("startDate", response.getStartDate());
        result.put("endDate", response.getEndDate());
        result.put("isRecurring", response.getIsRecurring());
        result.put("message", "Ngân sách đã được tạo thành công.");
        return ResponseEntity.ok(result);
    }
}
