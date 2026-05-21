package com.finflow.backend.finance.wealth.infrastructure.adapter;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.finance.wealth.api.WealthAccountApi;
import com.finflow.backend.finance.wealth.domain.entity.WealthAccount;
import com.finflow.backend.finance.wealth.domain.entity.WealthAccountType;
import com.finflow.backend.finance.wealth.domain.repository.WealthAccountRepository;
import com.finflow.backend.finance.wealth.domain.repository.WealthAccountTypeRepository;
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

    private static final String BROKERAGE_CODE = "BROKERAGE";

    private final WealthAccountRepository wealthAccountRepository;
    private final WealthAccountTypeRepository wealthAccountTypeRepository;

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

    @Override
    public void updateBalance(String userId, UUID accountId, BigDecimal newBalance) {
        WealthAccount account = wealthAccountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new AppException(WealthErrorCode.WEALTH_ACCOUNT_NOT_FOUND));
        account.setBalance(newBalance);
        wealthAccountRepository.save(account);
    }

    @Override
    public AccountSnapshot createBrokerageAccount(String userId, String name) {
        WealthAccountType brokerageType = wealthAccountTypeRepository.findByCode(BROKERAGE_CODE)
                .orElseThrow(() -> new AppException(WealthErrorCode.WEALTH_ACCOUNT_TYPE_NOT_FOUND));
        WealthAccount account = WealthAccount.builder()
                .userId(userId)
                .name(name)
                .wealthAccountType(brokerageType)
                .balance(BigDecimal.ZERO)
                .isSynced(false)
                .includeInNetWorth(true)
                .build();
        return toSnapshot(wealthAccountRepository.save(account));
    }

    @Override
    public AccountSnapshot requireBrokerageAccount(String userId, UUID accountId) {
        AccountSnapshot account = findAccountWithType(userId, accountId)
                .orElseThrow(() -> new AppException(WealthErrorCode.WEALTH_ACCOUNT_NOT_FOUND));
        if (!BROKERAGE_CODE.equals(account.typeCode())) {
            throw new AppException(WealthErrorCode.WEALTH_ACCOUNT_NOT_ELIGIBLE);
        }
        return account;
    }

    private AccountSnapshot toSnapshot(WealthAccount account) {
        WealthAccountType type = account.getWealthAccountType();
        return new AccountSnapshot(
                account.getId(),
                account.getName(),
                type == null ? null : type.getCode(),
                type == null ? null : type.getDisplayName(),
                account.getBalance(),
                type != null && Boolean.TRUE.equals(type.getIsTransactionEligible()),
                type != null && Boolean.TRUE.equals(type.getIsDebt())
        );
    }
}
