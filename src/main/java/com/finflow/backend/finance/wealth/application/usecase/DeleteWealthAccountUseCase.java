package com.finflow.backend.finance.wealth.application.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.finance.transaction.domain.repository.TransactionRepository;
import com.finflow.backend.finance.wealth.domain.entity.WealthAccount;
import com.finflow.backend.finance.wealth.domain.repository.WealthAccountRepository;
import com.finflow.backend.finance.wealth.exception.WealthErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeleteWealthAccountUseCase {

    private final WealthAccountRepository wealthAccountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public void execute(String userId, UUID accountId) {
        log.info("Deleting wealth account {} for user: {}", accountId, userId);

        WealthAccount account = wealthAccountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new AppException(WealthErrorCode.WEALTH_ACCOUNT_NOT_FOUND));

        if (transactionRepository.countByWealthAccount_Id(accountId) > 0) {
            throw new AppException(WealthErrorCode.WEALTH_ACCOUNT_HAS_TRANSACTIONS);
        }

        wealthAccountRepository.delete(account);
    }
}
