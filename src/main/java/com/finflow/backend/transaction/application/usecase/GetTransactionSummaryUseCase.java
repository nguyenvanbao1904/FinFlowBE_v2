package com.finflow.backend.transaction.application.usecase;

import com.finflow.backend.transaction.domain.repository.TransactionRepository;
import com.finflow.backend.transaction.presentation.response.TransactionSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetTransactionSummaryUseCase {

    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
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
