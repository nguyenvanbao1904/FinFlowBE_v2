package com.finflow.backend.finance.transaction.presentation.controller;

import com.finflow.backend.finance.transaction.application.command.CreateCategoryCommand;
import com.finflow.backend.finance.transaction.application.command.DeleteCategoryCommand;
import com.finflow.backend.finance.transaction.application.command.UpdateCategoryCommand;
import com.finflow.backend.finance.transaction.application.port.in.CreateCategoryPort;
import com.finflow.backend.finance.transaction.application.port.in.DeleteCategoryPort;
import com.finflow.backend.finance.transaction.application.port.in.GetCategoriesPort;
import com.finflow.backend.finance.transaction.application.port.in.UpdateCategoryPort;
import com.finflow.backend.finance.transaction.application.query.GetCategoriesQuery;
import com.finflow.backend.finance.transaction.presentation.mapper.TransactionPresentationMapper;
import com.finflow.backend.finance.transaction.presentation.request.CreateCategoryRequest;
import com.finflow.backend.finance.transaction.presentation.request.UpdateCategoryRequest;
import com.finflow.backend.finance.transaction.presentation.response.CategoryResponse;
import com.finflow.backend.common.versioning.ApiVersion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions/categories")
@RequiredArgsConstructor
@ApiVersion("1")
@Tag(name = "Category", description = "Transaction category management APIs")
public class CategoryController {

    private final GetCategoriesPort getCategoriesUseCase;
    private final CreateCategoryPort createCategoryUseCase;
    private final UpdateCategoryPort updateCategoryUseCase;
    private final DeleteCategoryPort deleteCategoryUseCase;
    private final TransactionPresentationMapper mapper;

    @Operation(summary = "Get all categories of current user (including system categories)")
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<CategoryResponse>> getCategories(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<CategoryResponse> response = getCategoriesUseCase.execute(new GetCategoriesQuery(userId))
                .stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create a new category")
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<java.util.Map<String, UUID>> createCategory(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateCategoryRequest request) {
        String userId = jwt.getSubject();
        var id = createCategoryUseCase.execute(
            new CreateCategoryCommand(userId, request.getName(), request.getType(), request.getIcon(), request.getColor())
        ).id();
        return ResponseEntity.status(HttpStatus.CREATED).body(java.util.Map.of("id", id));
    }

    @Operation(summary = "Update a category (own categories only)")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<java.util.Map<String, UUID>> updateCategory(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        String userId = jwt.getSubject();
        var updatedId = updateCategoryUseCase.execute(
            new UpdateCategoryCommand(userId, id, request.getName(), request.getIcon(), request.getColor())
        ).id();
        return ResponseEntity.ok(java.util.Map.of("id", updatedId));
    }

    @Operation(summary = "Delete a category (own categories only, not in use)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteCategory(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {
        String userId = jwt.getSubject();
        deleteCategoryUseCase.execute(new DeleteCategoryCommand(userId, id));
        return ResponseEntity.noContent().build();
    }
}
