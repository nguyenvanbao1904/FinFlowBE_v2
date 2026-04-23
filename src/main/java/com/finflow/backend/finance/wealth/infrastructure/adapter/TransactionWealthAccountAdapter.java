package com.finflow.backend.finance.wealth.infrastructure.adapter;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.finance.wealth.api.WealthAccountApi;
import com.finflow.backend.finance.wealth.domain.entity.WealthAccount;
import com.finflow.backend.finance.wealth.domain.repository.WealthAccountRepository;
import com.finflow.backend.finance.wealth.exception.WealthErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter owned by wealth module to provide wealth account data
 * for transaction use cases through wealth's public account API contract.
 */
@Component
@RequiredArgsConstructor
public class TransactionWealthAccountAdapter implements WealthAccountApi {

    private final WealthAccountRepository wealthAccountRepository;

    @Override
    public Optional<AccountSnapshot> findAccountWithType(String userId, UUID accountId) {
        return wealthAccountRepository.findByIdAndUserIdWithType(accountId, userId)
                .map(this::toSnapshot);
    }

    @Override
    public List<AccountSnapshot> findAllAccountsWithType(String userId) {
        return wealthAccountRepository.findAllByUserIdWithType(userId)
                .stream()
                .map(this::toSnapshot)
                .toList();
    }

    @Override
    public void updateBalance(UUID accountId, BigDecimal newBalance) {
        WealthAccount account = wealthAccountRepository.findById(accountId)
                .orElseThrow(() -> new AppException(WealthErrorCode.WEALTH_ACCOUNT_NOT_FOUND));
        account.setBalance(newBalance);
        wealthAccountRepository.save(account);
    }

    private AccountSnapshot toSnapshot(WealthAccount account) {
        return new AccountSnapshot(
                account.getId(),
                account.getName(),
                account.getWealthAccountType() == null ? null : account.getWealthAccountType().getDisplayName(),
                account.getBalance(),
                account.getWealthAccountType() != null && Boolean.TRUE.equals(account.getWealthAccountType().getIsTransactionEligible()),
                account.getWealthAccountType() != null && Boolean.TRUE.equals(account.getWealthAccountType().getIsDebt())
        );
    }
}
