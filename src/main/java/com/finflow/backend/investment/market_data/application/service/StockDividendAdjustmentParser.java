package com.finflow.backend.investment.market_data.application.usecase;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse hệ số nhân số cổ phiếu từ cổ tức dạng cổ phiếu (STOCK), vd +20% → 1.2.
 */
@Component
public class StockDividendAdjustmentParser {

    private static final Pattern PCT = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*%");
    private static final Pattern RATIO_COLON = Pattern.compile("(\\d+)\\s*:\\s*(\\d+)");

    /**
     * @return hệ số mới/cũ (vd 1.2), empty nếu không parse được.
     */
    public Optional<Double> parseMultiplier(String ratio, String eventTitle) {
        String a = ratio == null ? "" : ratio.trim();
        String b = eventTitle == null ? "" : eventTitle.trim();
        Optional<Double> fromRatio = parseFromText(a);
        if (fromRatio.isPresent()) {
            return fromRatio;
        }
        return parseFromText(b);
    }

    private static Optional<Double> parseFromText(String text) {
        if (text.isEmpty()) {
            return Optional.empty();
        }
        Matcher mPct = PCT.matcher(text);
        if (mPct.find()) {
            double p = parseDecimal(mPct.group(1));
            if (p > 0 && p < 500) {
                return Optional.of(1.0 + p / 100.0);
            }
        }
        Matcher mCol = RATIO_COLON.matcher(text.replace(",", ""));
        if (mCol.find()) {
            double num = parseDecimal(mCol.group(1));
            double den = parseDecimal(mCol.group(2));
            if (den > 0 && num >= 0) {
                return Optional.of((num + den) / den);
            }
        }
        return Optional.empty();
    }

    private static double parseDecimal(String s) {
        try {
            return Double.parseDouble(s.replace(',', '.'));
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }
}
