package com.finflow.backend.finance.transaction.application.port.in;

import com.finflow.backend.finance.transaction.presentation.response.TransactionResponse;
import org.springframework.data.domain.Page;
import java.time.LocalDate;

public interface GetTransactionsPort {
    Page<TransactionResponse> execute(String userId, int page, int size, LocalDate startDate, LocalDate endDate, String keyword);
}
