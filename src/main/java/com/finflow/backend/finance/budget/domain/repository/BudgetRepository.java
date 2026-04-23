package com.finflow.backend.finance.budget.domain.repository;

import com.finflow.backend.finance.budget.domain.entity.Budget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    List<Budget> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * For roll-forward job: only recurring budgets whose endDate falls in the given window.
     * Use with index (user_id, is_recurring, end_date).
     */
    Page<Budget> findByIsRecurringTrueAndEndDateBetween(LocalDate startInclusive, LocalDate endInclusive, Pageable pageable);

    /**
     * Bulk lookup for roll-forward dedup: fetch existing budgets that match any of the given
     * (userId, categoryId, startDate) combinations so we can filter in-memory rather than
     * issuing one existsBy query per budget.
     */
    List<Budget> findByUserIdInAndCategoryIdInAndStartDateIn(
            Collection<String> userIds,
            Collection<UUID> categoryIds,
            Collection<LocalDate> startDates);

    long countByCategoryId(UUID categoryId);
}
