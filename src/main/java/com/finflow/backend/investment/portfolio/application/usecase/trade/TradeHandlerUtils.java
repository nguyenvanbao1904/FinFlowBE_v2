package com.finflow.backend.investment.portfolio.application.usecase.trade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

/**
 * Package-private utilities shared by all {@link TradeHandler} implementations.
 */
final class TradeHandlerUtils {

    private TradeHandlerUtils() {}

    static BigDecimal toScale2(BigDecimal v) {
        return v == null ? BigDecimal.ZERO.setScale(2) : v.setScale(2, RoundingMode.HALF_UP);
    }

    static LocalDateTime parseDateOrNow(String transactionDate) {
        if (transactionDate == null || transactionDate.isBlank()) return LocalDateTime.now();
        try {
            return OffsetDateTime.parse(transactionDate).toLocalDateTime();
        } catch (DateTimeParseException ex) {
            try {
                return LocalDateTime.parse(transactionDate);
            } catch (DateTimeParseException ex2) {
                return LocalDateTime.now();
            }
        }
    }
}
