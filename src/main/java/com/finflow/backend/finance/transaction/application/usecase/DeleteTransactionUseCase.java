package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.finance.transaction.domain.entity.Transaction;
import com.finflow.backend.finance.transaction.domain.enums.CategoryType;
import com.finflow.backend.finance.transaction.domain.repository.TransactionRepository;
import com.finflow.backend.finance.transaction.exception.TransactionErrorCode;
import com.finflow.backend.finance.wealth.domain.repository.WealthAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final WealthAccountRepository wealthAccountRepository;

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public void execute(String userId, UUID transactionId) {
        log.info("Deleting transaction {} for userId: {}", transactionId, userId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new AppException(TransactionErrorCode.TRANSACTION_NOT_FOUND));

        if (!transaction.getUserId().equals(userId)) {
            log.warn("User {} attempted to delete transaction {} owned by {}",
                    userId, transactionId, transaction.getUserId());
            throw new AppException(TransactionErrorCode.UNAUTHORIZED_ACCESS);
        }

        var account = transaction.getWealthAccount();
        if (transaction.getType() == CategoryType.INCOME) {
            account.setBalance(account.getBalance().subtract(transaction.getAmount()));
        } else {
            account.setBalance(account.getBalance().add(transaction.getAmount()));
        }
        wealthAccountRepository.save(account);

        transactionRepository.delete(transaction);
        log.info("Transaction {} deleted successfully", transactionId);
    }
}
