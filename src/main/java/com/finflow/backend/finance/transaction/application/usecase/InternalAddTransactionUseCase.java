package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.finance.transaction.application.command.AddTransactionCommand;
import com.finflow.backend.finance.transaction.application.mapper.TransactionMapper;
import com.finflow.backend.finance.transaction.application.port.in.InternalAddTransactionPort;
import com.finflow.backend.finance.transaction.domain.entity.Category;
import com.finflow.backend.finance.transaction.domain.entity.Transaction;
import com.finflow.backend.finance.transaction.domain.enums.CategoryType;
import com.finflow.backend.finance.transaction.domain.repository.CategoryRepository;
import com.finflow.backend.finance.transaction.domain.repository.TransactionRepository;
import com.finflow.backend.finance.transaction.exception.TransactionErrorCode;
import com.finflow.backend.finance.transaction.presentation.response.TransactionResponse;
import com.finflow.backend.finance.wealth.domain.entity.WealthAccount;
import com.finflow.backend.finance.wealth.domain.repository.WealthAccountRepository;
import com.finflow.backend.finance.wealth.exception.WealthErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Internal version of {@link AddTransactionUseCase} — called by AI agent via internal API.
 * NO {@code @PreAuthorize} — security is handled by X-Internal-Api-Key at the filter level.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InternalAddTransactionUseCase implements InternalAddTransactionPort {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;
    private final WealthAccountRepository wealthAccountRepository;

    private static final ZoneId UTC = ZoneId.of("UTC");

    @Transactional
    @Override
    public TransactionResponse execute(AddTransactionCommand command) {
        String userId = command.userId();
        log.info("[INTERNAL] Adding transaction for userId: {}", userId);

        Category category = categoryRepository.findByIdAndUserIdOrSystem(command.categoryId(), userId)
                .orElseThrow(() -> new AppException(TransactionErrorCode.CATEGORY_NOT_FOUND));

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

        LocalDateTime transactionDateUTC;
        try {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(command.transactionDate(), DateTimeFormatter.ISO_DATE_TIME);
            transactionDateUTC = zonedDateTime.withZoneSameInstant(UTC).toLocalDateTime();
        } catch (DateTimeParseException e) {
            log.error("[INTERNAL] Failed to parse transactionDate: {}", command.transactionDate(), e);
            throw new AppException(TransactionErrorCode.INVALID_TRANSACTION_DATE);
        }

        Transaction transaction = Transaction.builder()
                .userId(userId)
                .amount(amount)
                .type(command.type())
                .category(category)
                .note(command.note())
                .wealthAccount(account)
                .transactionDate(transactionDateUTC)
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
