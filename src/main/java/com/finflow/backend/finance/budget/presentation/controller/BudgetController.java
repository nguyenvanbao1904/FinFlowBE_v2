package com.finflow.backend.finance.budget.presentation.controller;

import com.finflow.backend.finance.budget.application.command.CreateBudgetCommand;
import com.finflow.backend.finance.budget.application.command.DeleteBudgetCommand;
import com.finflow.backend.finance.budget.application.command.UpdateBudgetCommand;
import com.finflow.backend.common.versioning.ApiVersion;
import com.finflow.backend.finance.budget.application.port.in.CreateBudgetPort;
import com.finflow.backend.finance.budget.application.port.in.DeleteBudgetPort;
import com.finflow.backend.finance.budget.application.port.in.GetBudgetsPort;
import com.finflow.backend.finance.budget.application.port.in.UpdateBudgetPort;
import com.finflow.backend.finance.budget.application.query.GetBudgetsQuery;
import com.finflow.backend.finance.budget.presentation.mapper.BudgetPresentationMapper;
import com.finflow.backend.finance.budget.presentation.request.CreateBudgetRequest;
import com.finflow.backend.finance.budget.presentation.request.UpdateBudgetRequest;
import com.finflow.backend.finance.budget.presentation.response.BudgetResponse;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/budgets")
@ApiVersion("1")
@RequiredArgsConstructor
@Tag(name = "Budget", description = "Budget management APIs")
public class BudgetController {

    private final GetBudgetsPort getBudgetsUseCase;
    private final CreateBudgetPort createBudgetUseCase;
    private final UpdateBudgetPort updateBudgetUseCase;
    private final DeleteBudgetPort deleteBudgetUseCase;
    private final BudgetPresentationMapper mapper;

    @Operation(summary = "Get all budgets of current user")
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<BudgetResponse>> getBudgets(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(mapper.toResponses(getBudgetsUseCase.execute(new GetBudgetsQuery(userId))));
    }

    @Operation(summary = "Create a new budget")
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, UUID>> createBudget(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateBudgetRequest request
    ) {
        String userId = jwt.getSubject();
        var id = createBudgetUseCase.execute(
            new CreateBudgetCommand(userId, request.getCategoryId(), request.getTargetAmount(),
                request.getStartDate(), request.getEndDate(), request.getIsRecurring(), request.getRecurringStartDate())
        ).id();
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    @Operation(summary = "Update budget")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, UUID>> updateBudget(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBudgetRequest request
    ) {
        String userId = jwt.getSubject();
        var updatedId = updateBudgetUseCase.execute(
            new UpdateBudgetCommand(userId, id, request.getCategoryId(), request.getTargetAmount(),
                request.getStartDate(), request.getEndDate(), request.getIsRecurring(), request.getRecurringStartDate())
        ).id();
        return ResponseEntity.ok(Map.of("id", updatedId));
    }

    @Operation(summary = "Delete budget")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteBudget(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        String userId = jwt.getSubject();
        deleteBudgetUseCase.execute(new DeleteBudgetCommand(userId, id));
        return ResponseEntity.noContent().build();
    }
}
