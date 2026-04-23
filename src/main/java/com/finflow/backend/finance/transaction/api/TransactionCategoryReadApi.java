package com.finflow.backend.finance.transaction.api;

import com.finflow.backend.finance.common.enums.CategoryType;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Public category read contract exposed by transaction submodule.
 */
public interface TransactionCategoryReadApi {

    record CategorySnapshot(
            UUID id,
            String name,
            CategoryType type,
            String icon,
            String color,
            boolean systemCategory
    ) {}

    boolean isExpenseCategoryOfUserOrSystem(UUID categoryId, String userId);

    CategorySnapshot findCategory(UUID categoryId);

    /**
     * Batch-load categories by their IDs in a single query.
     * Missing IDs are silently omitted from the result map.
     */
    Map<UUID, CategorySnapshot> findCategoriesByIds(Collection<UUID> categoryIds);
}
