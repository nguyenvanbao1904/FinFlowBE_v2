package com.finflow.backend.finance.wealth.presentation.controller;

import com.finflow.backend.finance.wealth.application.command.CreateWealthAccountCommand;
import com.finflow.backend.finance.wealth.application.port.in.CreateWealthAccountPort;
import com.finflow.backend.finance.wealth.application.port.in.GetWealthAccountTypesPort;
import com.finflow.backend.finance.wealth.application.query.GetWealthAccountTypesQuery;
import com.finflow.backend.finance.wealth.presentation.mapper.WealthPresentationMapper;
import com.finflow.backend.finance.wealth.presentation.request.CreateWealthAccountRequest;
import com.finflow.backend.finance.wealth.presentation.response.WealthAccountTypeOptionResponse;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/internal/wealth")
@RequiredArgsConstructor
public class InternalWealthController {

    private final GetWealthAccountTypesPort getWealthAccountTypesUseCase;
    private final CreateWealthAccountPort createWealthAccountUseCase;
    private final WealthPresentationMapper mapper;

    @Operation(summary = "Get all wealth account types (internal)")
    @GetMapping("/account-types")
    public ResponseEntity<List<WealthAccountTypeOptionResponse>> getAccountTypes() {
        return ResponseEntity.ok(mapper.toTypeResponses(
                getWealthAccountTypesUseCase.execute(new GetWealthAccountTypesQuery())));
    }

    @Operation(summary = "Create wealth account on behalf of user (internal)")
    @PostMapping("/create-account")
    public ResponseEntity<Map<String, Object>> createAccount(
            @RequestParam String userId,
            @Valid @RequestBody CreateWealthAccountRequest request) {
        var output = createWealthAccountUseCase.execute(
                new CreateWealthAccountCommand(
                        userId,
                        request.getName(),
                        request.getAccountTypeId(),
                        request.getBalance(),
                        request.getIncludeInNetWorth()));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "OK");
        result.put("accountId", output.id());
        result.put("name", output.name());
        result.put("message", "Tài khoản đã được tạo thành công.");
        return ResponseEntity.ok(result);
    }
}
