package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.finance.transaction.application.mapper.TransactionMapper;
import com.finflow.backend.finance.transaction.domain.entity.Category;
import com.finflow.backend.finance.transaction.domain.entity.Transaction;
import com.finflow.backend.finance.transaction.domain.enums.CategoryType;
import com.finflow.backend.finance.transaction.domain.repository.CategoryRepository;
import com.finflow.backend.finance.transaction.domain.repository.TransactionRepository;
import com.finflow.backend.finance.transaction.exception.TransactionErrorCode;
import com.finflow.backend.finance.wealth.domain.entity.WealthAccount;
import com.finflow.backend.finance.wealth.domain.repository.WealthAccountRepository;
import com.finflow.backend.finance.wealth.exception.WealthErrorCode;
import com.finflow.backend.finance.transaction.presentation.request.UpdateTransactionRequest;
import com.finflow.backend.finance.transaction.presentation.response.TransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final WealthAccountRepository wealthAccountRepository;

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

        // 3.5. Validate account ownership and transaction eligibility
        WealthAccount account = wealthAccountRepository.findByIdAndUserIdWithType(request.getAccountId(), userId)
                .orElseThrow(() -> new AppException(WealthErrorCode.WEALTH_ACCOUNT_NOT_FOUND));
        if (Boolean.FALSE.equals(account.getWealthAccountType().getIsTransactionEligible())) {
            throw new AppException(WealthErrorCode.WEALTH_ACCOUNT_NOT_TRANSACTION_ELIGIBLE);
        }

        BigDecimal newAmount = request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO;
        boolean newIsDeduction = request.getType() == CategoryType.EXPENSE || request.getType() == CategoryType.SAVING;

        // Revert old transaction effect from old account
        WealthAccount oldAccount = transaction.getWealthAccount();
        if (transaction.getType() == CategoryType.INCOME) {
            oldAccount.setBalance(oldAccount.getBalance().subtract(transaction.getAmount()));
        } else {
            oldAccount.setBalance(oldAccount.getBalance().add(transaction.getAmount()));
        }
        wealthAccountRepository.save(oldAccount);

        WealthAccount accountToUpdate = account.getId().equals(oldAccount.getId()) ? oldAccount : account;
        if (newIsDeduction && Boolean.FALSE.equals(accountToUpdate.getWealthAccountType().getIsDebt())) {
            BigDecimal newBalance = accountToUpdate.getBalance().subtract(newAmount);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new AppException(TransactionErrorCode.INSUFFICIENT_BALANCE);
            }
        }

        // 4. Parse and convert transaction date to UTC
        LocalDateTime transactionDateUTC;
        try {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(request.getTransactionDate(), DateTimeFormatter.ISO_DATE_TIME);
            transactionDateUTC = zonedDateTime.withZoneSameInstant(UTC).toLocalDateTime();
            log.debug("Parsed transactionDate: {} (original timezone) -> {} (UTC)", 
                     request.getTransactionDate(), transactionDateUTC);
        } catch (DateTimeParseException e) {
            log.error("Failed to parse transactionDate: {}", request.getTransactionDate(), e);
            throw new AppException(TransactionErrorCode.INVALID_TRANSACTION_DATE);
        }

        // 5. Apply new transaction effect to (new) account
        if (request.getType() == CategoryType.INCOME) {
            accountToUpdate.setBalance(accountToUpdate.getBalance().add(newAmount));
        } else {
            accountToUpdate.setBalance(accountToUpdate.getBalance().subtract(newAmount));
        }
        wealthAccountRepository.save(accountToUpdate);

        // 6. Update transaction fields
        transaction.setAmount(newAmount);
        transaction.setType(request.getType());
        transaction.setCategory(category);
        transaction.setNote(request.getNote());
        transaction.setWealthAccount(accountToUpdate);
        transaction.setTransactionDate(transactionDateUTC);

        Transaction updatedTransaction = transactionRepository.save(transaction);
        return transactionMapper.toTransactionResponse(updatedTransaction);
    }
}
