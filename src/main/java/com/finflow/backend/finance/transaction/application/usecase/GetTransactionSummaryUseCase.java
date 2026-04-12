package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.finance.transaction.application.port.in.GetTransactionSummaryPort;
import com.finflow.backend.finance.transaction.domain.repository.TransactionRepository;
import com.finflow.backend.finance.transaction.presentation.response.TransactionSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetTransactionSummaryUseCase implements GetTransactionSummaryPort {

    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Override
    public TransactionSummaryResponse execute(String userId) {
        log.info("Calculating transaction summary for userId: {}", userId);

        BigDecimal totalIncome = transactionRepository.sumIncomeByUserId(userId);
        BigDecimal totalExpense = transactionRepository.sumExpenseByUserId(userId);
        BigDecimal totalBalance = totalIncome.subtract(totalExpense);

        return TransactionSummaryResponse.builder()
                .totalBalance(totalBalance)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .build();
    }
}
