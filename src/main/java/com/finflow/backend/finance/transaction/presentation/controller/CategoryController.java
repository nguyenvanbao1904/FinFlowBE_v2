package com.finflow.backend.finance.transaction.presentation.controller;

import com.finflow.backend.finance.transaction.application.command.CreateCategoryCommand;
import com.finflow.backend.finance.transaction.application.command.DeleteCategoryCommand;
import com.finflow.backend.finance.transaction.application.command.UpdateCategoryCommand;
import com.finflow.backend.finance.transaction.application.port.in.CreateCategoryPort;
import com.finflow.backend.finance.transaction.application.port.in.DeleteCategoryPort;
import com.finflow.backend.finance.transaction.application.port.in.GetCategoriesPort;
import com.finflow.backend.finance.transaction.application.port.in.UpdateCategoryPort;
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

    @Operation(summary = "Get all categories of current user (including system categories)")
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getCategories(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<CategoryResponse> response = getCategoriesUseCase.execute(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create a new category")
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateCategoryRequest request) {
        String userId = jwt.getSubject();
        CategoryResponse response = createCategoryUseCase.execute(
            new CreateCategoryCommand(userId, request.getName(), request.getType(), request.getIcon(), request.getColor())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update a category (own categories only)")
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        String userId = jwt.getSubject();
        CategoryResponse response = updateCategoryUseCase.execute(
            new UpdateCategoryCommand(userId, id, request.getName(), request.getIcon(), request.getColor())
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete a category (own categories only, not in use)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {
        String userId = jwt.getSubject();
        deleteCategoryUseCase.execute(new DeleteCategoryCommand(userId, id));
        return ResponseEntity.noContent().build();
    }
}
