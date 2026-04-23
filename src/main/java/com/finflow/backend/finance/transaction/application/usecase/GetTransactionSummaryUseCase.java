package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.finance.transaction.application.dto.TransactionSummaryOutput;
import com.finflow.backend.finance.transaction.application.port.in.GetTransactionSummaryPort;
import com.finflow.backend.finance.transaction.application.query.GetTransactionSummaryQuery;
import com.finflow.backend.finance.transaction.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetTransactionSummaryUseCase implements GetTransactionSummaryPort {

    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    @Override
    public TransactionSummaryOutput execute(GetTransactionSummaryQuery request) {
        String userId = request.userId();
        log.info("Calculating transaction summary for userId: {}", userId);

        BigDecimal totalIncome = transactionRepository.sumIncomeByUserId(userId);
        BigDecimal totalExpense = transactionRepository.sumExpenseByUserId(userId);
        BigDecimal totalBalance = totalIncome.subtract(totalExpense);

        return TransactionSummaryOutput.builder()
                .totalBalance(totalBalance)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .build();
    }
}
