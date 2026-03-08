package com.finflow.backend.transaction.application.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.transaction.domain.entity.Transaction;
import com.finflow.backend.transaction.domain.repository.TransactionRepository;
import com.finflow.backend.transaction.exception.TransactionErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeleteTransactionUseCase {

    private final TransactionRepository transactionRepository;

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public void execute(String userId, UUID transactionId) {
        log.info("Deleting transaction {} for userId: {}", transactionId, userId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new AppException(TransactionErrorCode.TRANSACTION_NOT_FOUND));

        // Security: Verify ownership
        if (!transaction.getUserId().equals(userId)) {
            log.warn("User {} attempted to delete transaction {} owned by {}",
                    userId, transactionId, transaction.getUserId());
            throw new AccessDeniedException("Bạn không có quyền xóa giao dịch này");
        }

        transactionRepository.delete(transaction);
        log.info("Transaction {} deleted successfully", transactionId);
    }
}
