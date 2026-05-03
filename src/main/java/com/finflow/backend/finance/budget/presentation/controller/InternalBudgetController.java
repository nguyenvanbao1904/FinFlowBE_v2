package com.finflow.backend.finance.budget.presentation.controller;

import com.finflow.backend.finance.budget.application.port.in.InternalCreateBudgetPort;
import com.finflow.backend.finance.budget.application.port.in.InternalGetBudgetsPort;
import com.finflow.backend.finance.budget.application.port.in.UpdateBudgetPort;
import com.finflow.backend.finance.budget.application.port.in.DeleteBudgetPort;
import com.finflow.backend.finance.budget.application.command.InternalCreateBudgetCommand;
import com.finflow.backend.finance.budget.application.command.UpdateBudgetCommand;
import com.finflow.backend.finance.budget.application.command.DeleteBudgetCommand;
import com.finflow.backend.finance.budget.application.query.InternalGetBudgetsQuery;
import com.finflow.backend.finance.budget.presentation.mapper.BudgetPresentationMapper;
import com.finflow.backend.finance.budget.presentation.request.CreateBudgetRequest;
import com.finflow.backend.finance.budget.presentation.request.UpdateBudgetRequest;
import com.finflow.backend.finance.budget.presentation.response.BudgetResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Internal API for AI agent — no JWT; uses {@code X-Internal-Api-Key}.
 */
@RestController
@RequestMapping("/api/internal/budget")
@RequiredArgsConstructor
public class InternalBudgetController {

    private final InternalGetBudgetsPort internalGetBudgetsUseCase;
    private final InternalCreateBudgetPort internalCreateBudgetUseCase;
    private final UpdateBudgetPort updateBudgetUseCase;
    private final DeleteBudgetPort deleteBudgetUseCase;
    private final BudgetPresentationMapper mapper;

    @Operation(summary = "Get budgets for user (internal)")
    @GetMapping("/budgets")
    public ResponseEntity<List<BudgetResponse>> listBudgets(@RequestParam String userId) {
        return ResponseEntity.ok(mapper.toResponses(internalGetBudgetsUseCase.execute(new InternalGetBudgetsQuery(userId))));
    }

    /**
     * Creates a budget after the user confirmed details in chat.
     * Returns only the created budget ID plus a confirmation message.
     */
    @Operation(summary = "Create budget on behalf of user (internal)")
    @PostMapping("/create-budget")
    public ResponseEntity<Map<String, Object>> createBudget(
            @RequestParam String userId,
            @Valid @RequestBody CreateBudgetRequest request) {
        UUID budgetId = internalCreateBudgetUseCase.execute(new InternalCreateBudgetCommand(
                userId,
                request.getCategoryId(),
                request.getTargetAmount(),
                request.getStartDate(),
                request.getEndDate(),
                request.getIsRecurring(),
                request.getRecurringStartDate())).id();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "OK");
        result.put("budgetId", budgetId);
        result.put("message", "Ngân sách đã được tạo thành công.");
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Update budget on behalf of user (internal)")
    @PutMapping("/budgets/{budgetId}")
    public ResponseEntity<Map<String, Object>> updateBudget(
            @RequestParam String userId,
            @PathVariable UUID budgetId,
            @Valid @RequestBody UpdateBudgetRequest request) {
        updateBudgetUseCase.execute(new UpdateBudgetCommand(
                userId,
                budgetId,
                request.getCategoryId(),
                request.getTargetAmount(),
                request.getStartDate(),
                request.getEndDate(),
                request.getIsRecurring(),
                request.getRecurringStartDate()));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "OK");
        result.put("budgetId", budgetId);
        result.put("message", "Ngân sách đã được cập nhật thành công.");
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Delete budget on behalf of user (internal)")
    @DeleteMapping("/budgets/{budgetId}")
    public ResponseEntity<Map<String, Object>> deleteBudget(
            @RequestParam String userId,
            @PathVariable UUID budgetId) {
        deleteBudgetUseCase.execute(new DeleteBudgetCommand(userId, budgetId));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "OK");
        result.put("message", "Ngân sách đã được xóa thành công.");
        return ResponseEntity.ok(result);
    }
}
