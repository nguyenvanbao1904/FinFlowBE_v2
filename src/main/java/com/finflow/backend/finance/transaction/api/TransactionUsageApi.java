package com.finflow.backend.finance.transaction.api;

import java.util.UUID;

/**
 * Public usage contract exposed by transaction submodule.
 */
public interface TransactionUsageApi {
    long countTransactionsByWealthAccountId(UUID accountId);
}
