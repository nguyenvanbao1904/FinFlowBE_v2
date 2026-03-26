package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.presentation.response.InvestmentAnalysisResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

final class InvestmentAnalysisNumberUtils {
    private InvestmentAnalysisNumberUtils() {
    }

    static Double toDouble(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }

    /** SUM a nullable BigDecimal field across a list of items. Returns null if all values are null. */
    static <T> Double sumBigDecimals(List<T> items, Function<T, BigDecimal> getter) {
        BigDecimal sum = null;
        for (T item : items) {
            BigDecimal v = getter.apply(item);
            if (v != null) {
                sum = (sum == null) ? v : sum.add(v);
            }
        }
        return sum == null ? null : sum.doubleValue();
    }

    static <T> Map<Integer, T> keepLatestQuarterByYear(
            List<T> items,
            Function<T, Integer> yearFn,
            Function<T, Integer> quarterFn
    ) {
        Map<Integer, T> map = new HashMap<>();
        for (T item : items) {
            Integer year = yearFn.apply(item);
            T existing = map.get(year);
            if (existing == null || quarterFn.apply(item) >= quarterFn.apply(existing)) {
                map.put(year, item);
            }
        }
        return map;
    }

    static long normalizeLimit(Integer rawLimit) {
        if (rawLimit == null) {
            return Long.MAX_VALUE;
        }
        return Math.max(rawLimit, 0);
    }

    static LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDate.parse(raw);
        } catch (Exception ignored) {
            return null;
        }
    }

    static Integer extractDividendYear(InvestmentAnalysisResponse.DividendPoint p) {
        LocalDate d = extractDividendDateForSort(p);
        return d == null ? null : d.getYear();
    }

    static LocalDate extractDividendDateForSort(InvestmentAnalysisResponse.DividendPoint p) {
        LocalDate record = parseDate(p.recordDate());
        if (record != null) return record;
        LocalDate exright = parseDate(p.exrightDate());
        if (exright != null) return exright;
        return parseDate(p.issueDate());
    }
}

