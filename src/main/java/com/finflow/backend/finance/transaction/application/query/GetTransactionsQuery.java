package com.finflow.backend.finance.transaction.application.query;

import java.time.LocalDate;

public record GetTransactionsQuery(
        String userId,
        int page,
        int size,
        LocalDate startDate,
        LocalDate endDate,
        String keyword
) {}
