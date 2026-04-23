package com.finflow.backend.finance.transaction.infrastructure.adapter;

import com.finflow.backend.finance.transaction.api.TransactionCategoryReadApi;
import com.finflow.backend.finance.transaction.domain.constant.TransactionConstants;
import com.finflow.backend.finance.common.enums.CategoryType;
import com.finflow.backend.finance.transaction.domain.entity.Category;
import com.finflow.backend.finance.transaction.domain.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter owned by transaction module to provide category data
 * for budget use cases through transaction's public category API contract.
 */
@Component
@RequiredArgsConstructor
public class BudgetCategoryAdapter implements TransactionCategoryReadApi {

    private final CategoryRepository categoryRepository;

    @Override
    public boolean isExpenseCategoryOfUserOrSystem(UUID categoryId, String userId) {
        return categoryRepository.findByIdAndUserIdOrSystem(categoryId, userId)
                .map(category -> category.getType() == CategoryType.EXPENSE)
                .orElse(false);
    }

    @Override
    public CategorySnapshot findCategory(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .map(this::toSnapshot)
                .orElse(null);
    }

    @Override
    public Map<UUID, CategorySnapshot> findCategoriesByIds(Collection<UUID> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Map.of();
        }
        return categoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, this::toSnapshot));
    }

    private CategorySnapshot toSnapshot(Category category) {
        return new CategorySnapshot(
                category.getId(),
                category.getName(),
                category.getType(),
                category.getIcon(),
                category.getColor(),
                TransactionConstants.SYSTEM_USER_ID.equalsIgnoreCase(category.getUserId())
        );
    }
}
