package com.finflow.backend.finance.wealth.presentation.controller;

import com.finflow.backend.common.versioning.ApiVersion;
import com.finflow.backend.finance.wealth.application.usecase.CreateWealthAccountUseCase;
import com.finflow.backend.finance.wealth.application.usecase.DeleteWealthAccountUseCase;
import com.finflow.backend.finance.wealth.application.usecase.GetWealthAccountTypesUseCase;
import com.finflow.backend.finance.wealth.application.usecase.GetWealthAccountsUseCase;
import com.finflow.backend.finance.wealth.application.usecase.UpdateWealthAccountUseCase;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/wealth/accounts")
@ApiVersion("1")
@RequiredArgsConstructor
@Tag(name = "WealthAccount", description = "Wealth account (wallet + asset) management APIs")
public class WealthAccountController {

    private final GetWealthAccountTypesUseCase getWealthAccountTypesUseCase;
    private final GetWealthAccountsUseCase getWealthAccountsUseCase;
    private final CreateWealthAccountUseCase createWealthAccountUseCase;
    private final UpdateWealthAccountUseCase updateWealthAccountUseCase;
    private final DeleteWealthAccountUseCase deleteWealthAccountUseCase;

    @Operation(summary = "Get all wealth account types (for pickers and transaction-eligibility)")
    @GetMapping("/types")
    public ResponseEntity<List<WealthAccountTypeOptionResponse>> getWealthAccountTypes() {
        return ResponseEntity.ok(getWealthAccountTypesUseCase.execute());
    }

    @Operation(summary = "Get all wealth accounts of current user")
    @GetMapping
    public ResponseEntity<List<WealthAccountResponse>> getWealthAccounts(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(getWealthAccountsUseCase.execute(userId));
    }

    @Operation(summary = "Create a new wealth account")
    @PostMapping
    public ResponseEntity<WealthAccountResponse> createWealthAccount(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateWealthAccountRequest request
    ) {
        String userId = jwt.getSubject();
        WealthAccountResponse response = createWealthAccountUseCase.execute(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update wealth account")
    @PutMapping("/{id}")
    public ResponseEntity<WealthAccountResponse> updateWealthAccount(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWealthAccountRequest request
    ) {
        String userId = jwt.getSubject();
        WealthAccountResponse response = updateWealthAccountUseCase.execute(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete wealth account")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWealthAccount(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        String userId = jwt.getSubject();
        deleteWealthAccountUseCase.execute(userId, id);
        return ResponseEntity.noContent().build();
    }
}
