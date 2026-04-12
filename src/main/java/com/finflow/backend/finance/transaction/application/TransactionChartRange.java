package com.finflow.backend.finance.transaction.application;

/**
 * Time bucket for {@link com.finflow.backend.finance.transaction.application.port.in.GetTransactionChartPort}.
 */
public enum TransactionChartRange {
    WEEK,
    MONTH,
    QUARTER,
    YEAR
}
