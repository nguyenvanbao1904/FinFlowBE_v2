package com.finflow.backend.transaction.application.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.transaction.application.mapper.TransactionMapper;
import com.finflow.backend.transaction.domain.entity.Category;
import com.finflow.backend.transaction.domain.entity.Transaction;
import com.finflow.backend.transaction.domain.repository.CategoryRepository;
import com.finflow.backend.transaction.domain.repository.TransactionRepository;
import com.finflow.backend.transaction.exception.TransactionErrorCode;
import com.finflow.backend.transaction.presentation.request.UpdateTransactionRequest;
import com.finflow.backend.transaction.presentation.response.TransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;

    private static final ZoneId UTC = ZoneId.of("UTC");

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public TransactionResponse execute(String userId, UUID transactionId, UpdateTransactionRequest request) {
        log.info("Updating transaction {} for userId: {}", transactionId, userId);

        // 1. Fetch transaction
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new AppException(TransactionErrorCode.TRANSACTION_NOT_FOUND));

        // 2. Security check: verify ownership
        if (!transaction.getUserId().equals(userId)) {
            log.warn("User {} attempted to update transaction {} owned by {}", 
                     userId, transactionId, transaction.getUserId());
            throw new AppException(TransactionErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 3. Validate category access
        Category category = categoryRepository.findByIdAndUserIdOrSystem(request.getCategoryId(), userId)
                .orElseThrow(() -> new AppException(TransactionErrorCode.CATEGORY_NOT_FOUND));

        // 4. Parse and convert transaction date to UTC
        LocalDateTime transactionDateUTC;
        try {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(request.getTransactionDate(), DateTimeFormatter.ISO_DATE_TIME);
            transactionDateUTC = zonedDateTime.withZoneSameInstant(UTC).toLocalDateTime();
            log.debug("Parsed transactionDate: {} (original timezone) -> {} (UTC)", 
                     request.getTransactionDate(), transactionDateUTC);
        } catch (DateTimeParseException e) {
            log.error("Failed to parse transactionDate: {}", request.getTransactionDate(), e);
            throw new AppException(TransactionErrorCode.INVALID_AMOUNT);  // Reuse existing error code
        }

        // 5. Update transaction fields
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setCategory(category);
        transaction.setNote(request.getNote());
        transaction.setTransactionDate(transactionDateUTC);
        // updatedAt is automatically handled by @UpdateTimestamp

        Transaction updatedTransaction = transactionRepository.save(transaction);
        return transactionMapper.toTransactionResponse(updatedTransaction);
    }
}
