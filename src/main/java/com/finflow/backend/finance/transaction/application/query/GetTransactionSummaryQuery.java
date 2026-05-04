package com.finflow.backend.finance.transaction.application.query;

import java.time.LocalDate;

public record GetTransactionSummaryQuery(
        String userId,
        LocalDate startDate,
        LocalDate endDate
) {
    public GetTransactionSummaryQuery(String userId) {
        this(userId, null, null);
    }
}
