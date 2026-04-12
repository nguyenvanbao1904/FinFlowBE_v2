package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.finance.transaction.application.port.in.AddTransactionPort;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.finance.transaction.application.mapper.TransactionMapper;
import com.finflow.backend.finance.wealth.domain.entity.WealthAccount;
import com.finflow.backend.finance.wealth.domain.repository.WealthAccountRepository;
import com.finflow.backend.finance.wealth.exception.WealthErrorCode;
import com.finflow.backend.finance.transaction.domain.entity.Category;
import com.finflow.backend.finance.transaction.domain.entity.Transaction;
import com.finflow.backend.finance.transaction.domain.enums.CategoryType;
import com.finflow.backend.finance.transaction.domain.repository.CategoryRepository;
import com.finflow.backend.finance.transaction.domain.repository.TransactionRepository;
import com.finflow.backend.finance.transaction.exception.TransactionErrorCode;
import com.finflow.backend.finance.transaction.application.command.AddTransactionCommand;
import com.finflow.backend.finance.transaction.presentation.response.TransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
@RequiredArgsConstructor
@Slf4j
public class AddTransactionUseCase implements AddTransactionPort {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;
    private final WealthAccountRepository wealthAccountRepository;

    private static final ZoneId UTC = ZoneId.of("UTC");

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Override
    public TransactionResponse execute(AddTransactionCommand command) {
        String userId = command.userId();
        log.info("Adding transaction for userId: {}", userId);

        Category category = categoryRepository.findByIdAndUserIdOrSystem(command.categoryId(), userId)
                .orElseThrow(() -> new AppException(TransactionErrorCode.CATEGORY_NOT_FOUND));

        // Validate account ownership and transaction eligibility
        WealthAccount account = wealthAccountRepository.findByIdAndUserIdWithType(command.accountId(), userId)
                .orElseThrow(() -> new AppException(WealthErrorCode.WEALTH_ACCOUNT_NOT_FOUND));
        if (Boolean.FALSE.equals(account.getWealthAccountType().getIsTransactionEligible())) {
            throw new AppException(WealthErrorCode.WEALTH_ACCOUNT_NOT_TRANSACTION_ELIGIBLE);
        }

        BigDecimal amount = command.amount() != null ? command.amount() : BigDecimal.ZERO;
        boolean isDeduction = command.type() == CategoryType.EXPENSE || command.type() == CategoryType.SAVING;
        if (isDeduction && Boolean.FALSE.equals(account.getWealthAccountType().getIsDebt())) {
            BigDecimal newBalance = account.getBalance().subtract(amount);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new AppException(TransactionErrorCode.INSUFFICIENT_BALANCE);
            }
        }

        // Parse ISO8601 string with timezone (e.g., 2026-03-05T18:09:41.830+07:00)
        // and convert to UTC before saving to DB
        LocalDateTime transactionDateUTC;
        try {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(command.transactionDate(), DateTimeFormatter.ISO_DATE_TIME);
            transactionDateUTC = zonedDateTime.withZoneSameInstant(UTC).toLocalDateTime();
            log.debug("Parsed transactionDate: {} (original timezone) -> {} (UTC)", 
                     command.transactionDate(), transactionDateUTC);
        } catch (DateTimeParseException e) {
            log.error("Failed to parse transactionDate: {}", command.transactionDate(), e);
            throw new AppException(TransactionErrorCode.INVALID_TRANSACTION_DATE);
        }

        Transaction transaction = Transaction.builder()
                .userId(userId)
                .amount(amount)
                .type(command.type())
                .category(category)
                .note(command.note())
                .wealthAccount(account)
                .transactionDate(transactionDateUTC)  // Save as UTC
                // createdAt and updatedAt are automatically handled by @CreationTimestamp and @UpdateTimestamp
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        if (command.type() == CategoryType.INCOME) {
            account.setBalance(account.getBalance().add(amount));
        } else {
            account.setBalance(account.getBalance().subtract(amount));
        }
        wealthAccountRepository.save(account);

        return transactionMapper.toTransactionResponse(savedTransaction);
    }
}
