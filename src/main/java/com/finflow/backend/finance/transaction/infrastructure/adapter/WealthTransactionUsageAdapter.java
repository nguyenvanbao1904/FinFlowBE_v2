package com.finflow.backend.finance.transaction.infrastructure.adapter;

import com.finflow.backend.finance.transaction.domain.repository.TransactionRepository;
import com.finflow.backend.finance.transaction.api.TransactionUsageApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adapter owned by transaction module to expose transaction usage data
 * for wealth use cases through transaction's public usage API contract.
 */
@Component
@RequiredArgsConstructor
public class WealthTransactionUsageAdapter implements TransactionUsageApi {

    private final TransactionRepository transactionRepository;

    @Override
    public long countTransactionsByWealthAccountId(UUID accountId) {
        return transactionRepository.countByWealthAccountId(accountId);
    }
}
