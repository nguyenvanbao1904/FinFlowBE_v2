package com.finflow.backend.finance.transaction.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Public read contract exposed by transaction submodule for other finance submodules.
 */
public interface TransactionReadApi {

    BigDecimal sumExpenseByUserIdAndCategoryIdBetween(
            String userId,
            UUID categoryId,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    );

    /**
     * Fetches all EXPENSE transactions for the given user whose category is in
     * {@code categoryIds} and whose transactionDate falls within
     * [{@code rangeStart}, {@code rangeEnd}).  The caller is responsible for
     * further filtering each row against individual per-budget date windows.
     *
     * <p>This is the batch alternative to calling
     * {@link #sumExpenseByUserIdAndCategoryIdBetween} once per budget, reducing
     * N round-trips to a single query.
     */
    List<ExpenseRow> findExpensesByUserIdAndCategoryIdsBetween(
            String userId,
            Collection<UUID> categoryIds,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd
    );

    /** Minimal projection used by the batch expense query. */
    record ExpenseRow(UUID categoryId, LocalDateTime transactionDate, BigDecimal amount) {}
}
