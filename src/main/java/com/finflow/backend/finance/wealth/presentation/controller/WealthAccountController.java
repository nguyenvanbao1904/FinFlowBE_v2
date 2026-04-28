package com.finflow.backend.finance.wealth.presentation.controller;

import com.finflow.backend.common.versioning.ApiVersion;
import com.finflow.backend.finance.wealth.application.command.CreateWealthAccountCommand;
import com.finflow.backend.finance.wealth.application.command.DeleteWealthAccountCommand;
import com.finflow.backend.finance.wealth.application.command.UpdateWealthAccountCommand;
import com.finflow.backend.finance.wealth.application.port.in.CreateWealthAccountPort;
import com.finflow.backend.finance.wealth.application.port.in.DeleteWealthAccountPort;
import com.finflow.backend.finance.wealth.application.port.in.GetWealthAccountTypesPort;
import com.finflow.backend.finance.wealth.application.port.in.GetWealthAccountsPort;
import com.finflow.backend.finance.wealth.application.port.in.UpdateWealthAccountPort;
import com.finflow.backend.finance.wealth.application.query.GetWealthAccountTypesQuery;
import com.finflow.backend.finance.wealth.application.query.GetWealthAccountsQuery;
import com.finflow.backend.finance.wealth.presentation.mapper.WealthPresentationMapper;
import com.finflow.backend.finance.wealth.presentation.request.CreateWealthAccountRequest;
import com.finflow.backend.finance.wealth.presentation.request.UpdateWealthAccountRequest;
import com.finflow.backend.finance.wealth.presentation.response.WealthAccountTypeOptionResponse;
import com.finflow.backend.finance.wealth.presentation.response.WealthAccountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/wealth/accounts")
@ApiVersion("1")
@RequiredArgsConstructor
@Tag(name = "WealthAccount", description = "Wealth account (wallet + asset) management APIs")
public class WealthAccountController {

    private final GetWealthAccountTypesPort getWealthAccountTypesUseCase;
    private final GetWealthAccountsPort getWealthAccountsUseCase;
    private final CreateWealthAccountPort createWealthAccountUseCase;
    private final UpdateWealthAccountPort updateWealthAccountUseCase;
    private final DeleteWealthAccountPort deleteWealthAccountUseCase;
    private final WealthPresentationMapper mapper;

    @Operation(summary = "Get all wealth account types (for pickers and transaction-eligibility)")
    @GetMapping("/types")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<WealthAccountTypeOptionResponse>> getWealthAccountTypes() {
        return ResponseEntity.ok(mapper.toTypeResponses(
                getWealthAccountTypesUseCase.execute(new GetWealthAccountTypesQuery())));
    }

    @Operation(summary = "Get all wealth accounts of current user")
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<WealthAccountResponse>> getWealthAccounts(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(mapper.toAccountResponses(
                getWealthAccountsUseCase.execute(new GetWealthAccountsQuery(userId))));
    }

    @Operation(summary = "Create a new wealth account")
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<WealthAccountResponse> createWealthAccount(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateWealthAccountRequest request
    ) {
        String userId = jwt.getSubject();
        var output = createWealthAccountUseCase.execute(
            new CreateWealthAccountCommand(userId, request.getName(), request.getAccountTypeId(),
                request.getBalance(), request.getIncludeInNetWorth())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(output));
    }

    @Operation(summary = "Update wealth account")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<WealthAccountResponse> updateWealthAccount(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWealthAccountRequest request
    ) {
        String userId = jwt.getSubject();
        var output = updateWealthAccountUseCase.execute(
            new UpdateWealthAccountCommand(userId, id, request.getName(), request.getAccountTypeId(),
                request.getBalance(), request.getIncludeInNetWorth())
        );
        return ResponseEntity.ok(mapper.toResponse(output));
    }

    @Operation(summary = "Delete wealth account")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteWealthAccount(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        String userId = jwt.getSubject();
        deleteWealthAccountUseCase.execute(new DeleteWealthAccountCommand(userId, id));
        return ResponseEntity.noContent().build();
    }
}
