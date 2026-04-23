package com.finflow.backend.finance.wealth.application.usecase;

import com.finflow.backend.finance.wealth.application.port.in.DeleteWealthAccountPort;
import com.finflow.backend.finance.transaction.api.TransactionUsageApi;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.finance.wealth.domain.entity.WealthAccount;
import com.finflow.backend.finance.wealth.domain.repository.WealthAccountRepository;
import com.finflow.backend.finance.wealth.exception.WealthErrorCode;
import com.finflow.backend.finance.wealth.application.command.DeleteWealthAccountCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeleteWealthAccountUseCase implements DeleteWealthAccountPort {

    private final WealthAccountRepository wealthAccountRepository;
    private final TransactionUsageApi transactionUsageApi;

    @Transactional
    @Override
    public void execute(DeleteWealthAccountCommand command) {
        String userId = command.userId();
        UUID accountId = command.accountId();
        log.info("Deleting wealth account {} for user: {}", accountId, userId);

        WealthAccount account = wealthAccountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new AppException(WealthErrorCode.WEALTH_ACCOUNT_NOT_FOUND));

        if (transactionUsageApi.countTransactionsByWealthAccountId(accountId) > 0) {
            throw new AppException(WealthErrorCode.WEALTH_ACCOUNT_HAS_TRANSACTIONS);
        }

        wealthAccountRepository.delete(account);
    }
}
