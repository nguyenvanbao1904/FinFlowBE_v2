package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.finance.transaction.application.port.in.DeleteTransactionPort;
import com.finflow.backend.finance.wealth.api.WealthAccountApi;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.finance.transaction.domain.entity.Transaction;
import com.finflow.backend.finance.common.enums.CategoryType;
import com.finflow.backend.finance.transaction.domain.repository.TransactionRepository;
import com.finflow.backend.finance.transaction.exception.TransactionErrorCode;
import com.finflow.backend.finance.transaction.application.command.DeleteTransactionCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeleteTransactionUseCase implements DeleteTransactionPort {

    private final TransactionRepository transactionRepository;
    private final WealthAccountApi wealthAccountApi;

    @Transactional
    @Override
    public void execute(DeleteTransactionCommand command) {
        String userId = command.userId();
        UUID transactionId = command.transactionId();
        log.info("Deleting transaction {} for userId: {}", transactionId, userId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new AppException(TransactionErrorCode.TRANSACTION_NOT_FOUND));

        if (!transaction.getUserId().equals(userId)) {
            log.warn("User {} attempted to delete transaction {} owned by {}",
                    userId, transactionId, transaction.getUserId());
            throw new AppException(TransactionErrorCode.UNAUTHORIZED_ACCESS);
        }

        UUID accountId = transaction.getWealthAccountId();
        WealthAccountApi.AccountSnapshot account = wealthAccountApi
                .findAccountWithType(userId, accountId)
                .orElseThrow(() -> new AppException(TransactionErrorCode.WEALTH_ACCOUNT_NOT_FOUND));
        if (transaction.getType() == CategoryType.INCOME) {
            wealthAccountApi.updateBalance(account.id(), account.balance().subtract(transaction.getAmount()));
        } else {
            wealthAccountApi.updateBalance(account.id(), account.balance().add(transaction.getAmount()));
        }

        transactionRepository.delete(transaction);
        log.info("Transaction {} deleted successfully", transactionId);
    }
}
