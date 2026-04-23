package com.finflow.backend.finance.wealth.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Public wealth-account contract exposed by wealth submodule.
 */
public interface WealthAccountApi {

    record AccountSnapshot(
            UUID id,
            String name,
            String typeDisplayName,
            BigDecimal balance,
            boolean transactionEligible,
            boolean debt
    ) {}

    Optional<AccountSnapshot> findAccountWithType(String userId, UUID accountId);

    List<AccountSnapshot> findAllAccountsWithType(String userId);

    void updateBalance(UUID accountId, BigDecimal newBalance);
}
