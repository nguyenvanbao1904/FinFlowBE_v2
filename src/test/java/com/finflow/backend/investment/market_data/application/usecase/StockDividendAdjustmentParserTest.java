package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.service.StockDividendAdjustmentParser;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StockDividendAdjustmentParserTest {

    private final StockDividendAdjustmentParser parser = new StockDividendAdjustmentParser();

    @Test
    void parsesPercentInRatio() {
        assertEquals(Optional.of(1.2), parser.parseMultiplier("20%", null));
        assertEquals(Optional.of(1.15), parser.parseMultiplier("15 %", null));
        assertEquals(Optional.of(1.055), parser.parseMultiplier("5,5%", null));
    }

    @Test
    void parsesPercentFromTitleWhenRatioBlank() {
        assertEquals(Optional.of(1.1), parser.parseMultiplier(null, "Cổ tức 10%"));
    }

    @Test
    void parsesColonRatio() {
        assertEquals(Optional.of(1.5), parser.parseMultiplier("1:2", null));
        assertEquals(Optional.of(11.0 / 10.0), parser.parseMultiplier("1 : 10", null));
    }

    @Test
    void emptyWhenUnparseable() {
        assertTrue(parser.parseMultiplier(null, null).isEmpty());
        assertTrue(parser.parseMultiplier("cash", "dividend").isEmpty());
    }
}
