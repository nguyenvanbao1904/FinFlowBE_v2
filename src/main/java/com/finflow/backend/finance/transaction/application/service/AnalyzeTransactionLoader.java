package com.finflow.backend.finance.transaction.application.service;

import com.finflow.backend.finance.transaction.domain.entity.Category;
import com.finflow.backend.finance.transaction.domain.entity.Transaction;
import com.finflow.backend.finance.transaction.domain.repository.CategoryRepository;
import com.finflow.backend.finance.transaction.domain.repository.TransactionRepository;
import com.finflow.backend.finance.wealth.api.WealthAccountApi;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Loads all DB data required by {@code AnalyzeTransactionUseCase} inside a proper
 * read-only transaction.
 *
 * <p>Extracted from {@code AnalyzeTransactionUseCase} to avoid the Spring CGLIB proxy
 * bypass that occurs with self-invocation ({@code this.loadDbData()}).  Because this
 * bean is injected into the use-case and called through its own proxy, the
 * {@code @Transactional(readOnly=true)} annotation is honoured by the transaction
 * interceptor.
 */
@Component
@RequiredArgsConstructor
public class AnalyzeTransactionLoader {

    private final CategoryRepository categoryRepository;
    private final WealthAccountApi wealthAccountApi;
    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public DbSnapshot load(String userId) {
        List<Category> categories = categoryRepository.findByUserIdOrSystem(userId);
        List<WealthAccountApi.AccountSnapshot> accounts = wealthAccountApi.findAllAccountsWithType(userId);
        List<Transaction> recentTransactions = transactionRepository
                .findByUserIdOrderByTransactionDateDescCreatedAtDesc(userId, PageRequest.of(0, 20))
                .getContent();
        return new DbSnapshot(categories, accounts, recentTransactions);
    }

    public record DbSnapshot(
            List<Category> categories,
            List<WealthAccountApi.AccountSnapshot> accounts,
            List<Transaction> recentTransactions
    ) {}
}
