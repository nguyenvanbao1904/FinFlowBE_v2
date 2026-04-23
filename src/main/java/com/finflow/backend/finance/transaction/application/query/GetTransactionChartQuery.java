package com.finflow.backend.finance.transaction.application.query;

import com.finflow.backend.finance.common.enums.TransactionChartRange;

import java.time.LocalDate;

public record GetTransactionChartQuery(
        String userId,
        TransactionChartRange range,
        LocalDate referenceDate
) {}
