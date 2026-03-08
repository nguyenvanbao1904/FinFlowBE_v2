package com.finflow.backend.transaction.presentation.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionSummaryResponse {
    BigDecimal totalBalance;
    BigDecimal totalIncome;
    BigDecimal totalExpense;
}
