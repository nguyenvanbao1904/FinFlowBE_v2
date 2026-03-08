package com.finflow.backend.transaction.application.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.transaction.application.mapper.TransactionMapper;
import com.finflow.backend.transaction.domain.entity.Category;
import com.finflow.backend.transaction.domain.entity.Transaction;
import com.finflow.backend.transaction.domain.repository.CategoryRepository;
import com.finflow.backend.transaction.domain.repository.TransactionRepository;
import com.finflow.backend.transaction.exception.TransactionErrorCode;
import com.finflow.backend.transaction.presentation.request.AddTransactionRequest;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class AddTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;

    private static final ZoneId UTC = ZoneId.of("UTC");

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public TransactionResponse execute(String userId, AddTransactionRequest request) {
        log.info("Adding transaction for userId: {}", userId);

        Category category = categoryRepository.findByIdAndUserIdOrSystem(request.getCategoryId(), userId)
                .orElseThrow(() -> new AppException(TransactionErrorCode.CATEGORY_NOT_FOUND));

        // Parse ISO8601 string with timezone (e.g., 2026-03-05T18:09:41.830+07:00)
        // and convert to UTC before saving to DB
        LocalDateTime transactionDateUTC;
        try {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(request.getTransactionDate(), DateTimeFormatter.ISO_DATE_TIME);
            transactionDateUTC = zonedDateTime.withZoneSameInstant(UTC).toLocalDateTime();
            log.debug("Parsed transactionDate: {} (original timezone) -> {} (UTC)", 
                     request.getTransactionDate(), transactionDateUTC);
        } catch (DateTimeParseException e) {
            log.error("Failed to parse transactionDate: {}", request.getTransactionDate(), e);
            throw new AppException(TransactionErrorCode.INVALID_AMOUNT);  // Reuse existing error code for now
        }

        Transaction transaction = Transaction.builder()
                .userId(userId)
                .amount(request.getAmount())
                .type(request.getType())
                .category(category)
                .note(request.getNote())
                .transactionDate(transactionDateUTC)  // Save as UTC
                // createdAt and updatedAt are automatically handled by @CreationTimestamp and @UpdateTimestamp
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        return transactionMapper.toTransactionResponse(savedTransaction);
    }
}
