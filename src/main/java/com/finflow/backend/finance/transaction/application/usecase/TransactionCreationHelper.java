package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.finance.transaction.application.command.AddTransactionCommand;
import com.finflow.backend.finance.transaction.domain.entity.Category;
import com.finflow.backend.finance.transaction.domain.entity.Transaction;
import com.finflow.backend.finance.common.enums.CategoryType;
import com.finflow.backend.finance.transaction.domain.repository.CategoryRepository;
import com.finflow.backend.finance.transaction.domain.repository.TransactionRepository;
import com.finflow.backend.finance.transaction.exception.TransactionErrorCode;
import com.finflow.backend.finance.wealth.api.WealthAccountApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Package-private helper that encapsulates the shared transaction-creation logic
 * reused by {@link AddTransactionUseCase} and {@link InternalAddTransactionUseCase}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
class TransactionCreationHelper {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final WealthAccountApi wealthAccountApi;

    /**
     * Validates the command, builds and persists the {@link Transaction}, and updates
     * the wealth account balance.  Returns the saved entity.
     *
     * @param command   the add-transaction command
     * @param logPrefix a short prefix for log messages (e.g. "" or "[INTERNAL] ")
     */
    Transaction createAndSave(AddTransactionCommand command, String logPrefix) {
        String userId = command.userId();
        log.info("{}Adding transaction for userId: {}", logPrefix, userId);

        Category category = categoryRepository.findByIdAndUserIdOrSystem(command.categoryId(), userId)
                .orElseThrow(() -> new AppException(TransactionErrorCode.CATEGORY_NOT_FOUND));

        WealthAccountApi.AccountSnapshot account = wealthAccountApi.findAccountWithType(userId, command.accountId())
                .orElseThrow(() -> new AppException(TransactionErrorCode.WEALTH_ACCOUNT_NOT_FOUND));
        if (!account.transactionEligible()) {
            throw new AppException(TransactionErrorCode.WEALTH_ACCOUNT_NOT_ELIGIBLE);
        }

        CategoryType categoryType = parseCategoryType(command.type(), TransactionErrorCode.INVALID_TRANSACTION_TYPE);
        BigDecimal amount = command.amount() != null ? command.amount() : BigDecimal.ZERO;
        boolean isDeduction = categoryType == CategoryType.EXPENSE || categoryType == CategoryType.SAVING;
        if (isDeduction && !account.debt()) {
            BigDecimal newBalance = account.balance().subtract(amount);
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new AppException(TransactionErrorCode.INSUFFICIENT_BALANCE);
            }
        }

        LocalDateTime transactionDateUTC = parseTransactionDate(command.transactionDate(), logPrefix);

        Transaction transaction = Transaction.builder()
                .userId(userId)
                .amount(amount)
                .type(categoryType)
                .category(category)
                .note(command.note())
                .transactionDate(transactionDateUTC)
                .build();
        transaction.setWealthAccountId(command.accountId());

        Transaction saved = transactionRepository.save(transaction);

        if (categoryType == CategoryType.INCOME) {
            wealthAccountApi.updateBalance(account.id(), account.balance().add(amount));
        } else {
            wealthAccountApi.updateBalance(account.id(), account.balance().subtract(amount));
        }

        return saved;
    }

    private CategoryType parseCategoryType(String type, TransactionErrorCode errorCode) {
        try {
            return CategoryType.valueOf(type);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid category type value: {}", type);
            throw new AppException(errorCode);
        }
    }

    private LocalDateTime parseTransactionDate(String transactionDate, String logPrefix) {
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(transactionDate, DateTimeFormatter.ISO_DATE_TIME);
            LocalDateTime utc = zdt.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
            log.debug("{}Parsed transactionDate: {} -> {} (UTC)", logPrefix, transactionDate, utc);
            return utc;
        } catch (DateTimeParseException e) {
            log.error("{}Failed to parse transactionDate: {}", logPrefix, transactionDate, e);
            throw new AppException(TransactionErrorCode.INVALID_TRANSACTION_DATE);
        }
    }
}
