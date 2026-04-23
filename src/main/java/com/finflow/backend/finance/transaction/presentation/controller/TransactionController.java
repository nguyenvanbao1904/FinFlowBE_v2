package com.finflow.backend.finance.transaction.presentation.controller;

import com.finflow.backend.finance.transaction.application.command.AddTransactionCommand;
import com.finflow.backend.finance.transaction.application.command.DeleteTransactionCommand;
import com.finflow.backend.finance.transaction.application.command.UpdateTransactionCommand;
import com.finflow.backend.finance.transaction.application.port.in.AddTransactionPort;
import com.finflow.backend.finance.transaction.application.port.in.DeleteTransactionPort;
import com.finflow.backend.finance.transaction.application.port.in.GetTransactionsPort;
import com.finflow.backend.finance.transaction.application.port.in.UpdateTransactionPort;
import com.finflow.backend.finance.transaction.application.query.GetTransactionsQuery;
import com.finflow.backend.finance.transaction.presentation.mapper.TransactionPresentationMapper;
import com.finflow.backend.finance.transaction.presentation.request.AddTransactionRequest;
import com.finflow.backend.finance.transaction.presentation.request.UpdateTransactionRequest;
import com.finflow.backend.finance.transaction.presentation.response.TransactionResponse;
import com.finflow.backend.common.versioning.ApiVersion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@ApiVersion("1")
@Tag(name = "Transaction", description = "Transaction CRUD APIs")
public class TransactionController {

    private final AddTransactionPort addTransactionUseCase;
    private final GetTransactionsPort getTransactionsUseCase;
    private final UpdateTransactionPort updateTransactionUseCase;
    private final DeleteTransactionPort deleteTransactionUseCase;
    private final TransactionPresentationMapper mapper;

    @Operation(summary = "Create a new transaction")
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<java.util.Map<String, UUID>> addTransaction(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody AddTransactionRequest request) {
        String userId = jwt.getSubject();
        var id = addTransactionUseCase.execute(
            new AddTransactionCommand(userId, request.getAmount(), request.getType(),
                request.getCategoryId(), request.getAccountId(), request.getNote(), request.getTransactionDate())
        ).id();
        return ResponseEntity.status(HttpStatus.CREATED).body(java.util.Map.of("id", id));
    }

    @Operation(summary = "Get paginated transactions with optional date range and keyword")
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
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
        var pageOut = getTransactionsUseCase
                .execute(new GetTransactionsQuery(userId, page, size, start, end, keyword));
        Page<TransactionResponse> response = new PageImpl<>(
                pageOut.content().stream().map(mapper::toResponse).toList(),
                PageRequest.of(pageOut.number(), pageOut.size()),
                pageOut.totalElements()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update a transaction")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<java.util.Map<String, UUID>> updateTransaction(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTransactionRequest request) {
        String userId = jwt.getSubject();
        var updatedId = updateTransactionUseCase.execute(
            new UpdateTransactionCommand(userId, id, request.getAmount(), request.getType(),
                request.getCategoryId(), request.getAccountId(), request.getNote(), request.getTransactionDate())
        ).id();
        return ResponseEntity.ok(java.util.Map.of("id", updatedId));
    }

    @Operation(summary = "Delete a transaction")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteTransaction(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {
        String userId = jwt.getSubject();
        deleteTransactionUseCase.execute(new DeleteTransactionCommand(userId, id));
        return ResponseEntity.noContent().build();
    }
}
