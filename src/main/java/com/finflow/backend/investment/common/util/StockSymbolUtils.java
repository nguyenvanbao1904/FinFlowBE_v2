package com.finflow.backend.investment.common.util;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Shared utilities for stock symbol normalization and numeric validation
 * used across portfolio and market-data modules.
 */
public final class StockSymbolUtils {

    private StockSymbolUtils() {
        // utility class
    }

    /**
     * Trims and upper-cases a raw symbol string.
     * Returns {@code null} if the input is null or blank after trimming.
     */
    public static String normalizeSymbol(String symbol) {
        if (symbol == null) return null;
        String s = symbol.trim().toUpperCase(Locale.ROOT);
        return s.isBlank() ? null : s;
    }

    /**
     * Returns {@code true} if the value is non-null and has no fractional part
     * (i.e. represents a whole number such as a stock quantity).
     */
    public static boolean isWholeNumber(BigDecimal v) {
        return v != null && v.stripTrailingZeros().scale() <= 0;
    }
}
