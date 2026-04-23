package com.finflow.backend.finance.transaction.infrastructure.adapter;

import com.finflow.backend.finance.transaction.api.TransactionReadApi;
import com.finflow.backend.finance.transaction.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter owned by transaction module to provide expense aggregates
 * for budget use cases through transaction's public read API contract.
 */
@Component
@RequiredArgsConstructor
public class BudgetTransactionAdapter implements TransactionReadApi {

    private final TransactionRepository transactionRepository;

    @Override
    public BigDecimal sumExpenseByUserIdAndCategoryIdBetween(
            String userId,
            UUID categoryId,
            LocalDateTime startInclusive,
            LocalDateTime endExclusive
    ) {
        return transactionRepository.sumExpenseByUserIdAndCategoryIdAndTransactionDateBetween(
                userId,
                categoryId,
                startInclusive,
                endExclusive
        );
    }

    @Override
    public List<ExpenseRow> findExpensesByUserIdAndCategoryIdsBetween(
            String userId,
            Collection<UUID> categoryIds,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd
    ) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return List.of();
        }
        return transactionRepository.findExpensesByUserIdAndCategoryIdsBetween(
                        userId, categoryIds, rangeStart, rangeEnd)
                .stream()
                .map(t -> new ExpenseRow(
                        t.getCategory().getId(),
                        t.getTransactionDate(),
                        t.getAmount()))
                .collect(Collectors.toList());
    }
}
