package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.finance.transaction.application.port.in.UpdateTransactionPort;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.finance.wealth.api.WealthAccountApi;
import com.finflow.backend.finance.transaction.domain.entity.Category;
import com.finflow.backend.finance.transaction.domain.entity.Transaction;
import com.finflow.backend.finance.common.enums.CategoryType;
import com.finflow.backend.finance.transaction.domain.repository.CategoryRepository;
import com.finflow.backend.finance.transaction.domain.repository.TransactionRepository;
import com.finflow.backend.finance.transaction.exception.TransactionErrorCode;
import com.finflow.backend.finance.transaction.application.command.UpdateTransactionCommand;
import com.finflow.backend.finance.transaction.application.dto.TransactionOutput;
import com.finflow.backend.finance.transaction.application.mapper.TransactionMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateTransactionUseCase implements UpdateTransactionPort {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final WealthAccountApi wealthAccountApi;
    private final TransactionMapper transactionMapper;

    private static final ZoneOffset UTC = ZoneOffset.UTC;

    @Transactional
    @Override
    public TransactionOutput execute(UpdateTransactionCommand command) {
        String userId = command.userId();
        UUID transactionId = command.transactionId();
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
        Category category = categoryRepository.findByIdAndUserIdOrSystem(command.categoryId(), userId)
                .orElseThrow(() -> new AppException(TransactionErrorCode.CATEGORY_NOT_FOUND));

        // 3.5. Validate account ownership and transaction eligibility
        WealthAccountApi.AccountSnapshot account = wealthAccountApi.findAccountWithType(userId, command.accountId())
                .orElseThrow(() -> new AppException(TransactionErrorCode.WEALTH_ACCOUNT_NOT_FOUND));
        if (!account.transactionEligible()) {
            throw new AppException(TransactionErrorCode.WEALTH_ACCOUNT_NOT_ELIGIBLE);
        }

        CategoryType newCategoryType;
        try {
            newCategoryType = CategoryType.valueOf(command.type());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid transaction type value: {}", command.type());
            throw new AppException(TransactionErrorCode.INVALID_TRANSACTION_TYPE);
        }

        BigDecimal newAmount = command.amount() != null ? command.amount() : BigDecimal.ZERO;
        boolean newIsDeduction = newCategoryType == CategoryType.EXPENSE || newCategoryType == CategoryType.SAVING;

        // Revert old transaction effect from old account
        UUID oldAccountId = transaction.getWealthAccountId();
        WealthAccountApi.AccountSnapshot oldAccount = wealthAccountApi
                .findAccountWithType(userId, oldAccountId)
                .orElseThrow(() -> new AppException(TransactionErrorCode.WEALTH_ACCOUNT_NOT_FOUND));
        if (transaction.getType() == CategoryType.INCOME) {
            wealthAccountApi.updateBalance(oldAccount.id(), oldAccount.balance().subtract(transaction.getAmount()));
        } else {
            wealthAccountApi.updateBalance(oldAccount.id(), oldAccount.balance().add(transaction.getAmount()));
        }

        WealthAccountApi.AccountSnapshot accountToUpdate = account.id().equals(oldAccount.id()) ? oldAccount : account;
        if (newIsDeduction && !accountToUpdate.debt()) {
            BigDecimal newBalance = accountToUpdate.balance().subtract(newAmount);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new AppException(TransactionErrorCode.INSUFFICIENT_BALANCE);
            }
        }

        // 4. Parse and convert transaction date to UTC
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

        // 5. Apply new transaction effect to (new) account
        if (newCategoryType == CategoryType.INCOME) {
            wealthAccountApi.updateBalance(accountToUpdate.id(), accountToUpdate.balance().add(newAmount));
        } else {
            wealthAccountApi.updateBalance(accountToUpdate.id(), accountToUpdate.balance().subtract(newAmount));
        }

        // 6. Update transaction fields
        transaction.setAmount(newAmount);
        transaction.setType(newCategoryType);
        transaction.setCategory(category);
        transaction.setNote(command.note());
        transaction.setWealthAccountId(accountToUpdate.id());
        transaction.setTransactionDate(transactionDateUTC);

        Transaction updatedTransaction = transactionRepository.save(transaction);
        return transactionMapper.toTransactionOutput(updatedTransaction);
    }
}
