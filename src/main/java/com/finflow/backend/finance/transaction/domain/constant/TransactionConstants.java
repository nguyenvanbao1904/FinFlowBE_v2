package com.finflow.backend.finance.transaction.domain.constant;

import java.time.ZoneOffset;

/**
 * Domain-level constants shared across the finance/transaction module.
 */
public final class TransactionConstants {

    private TransactionConstants() {
        // utility class
    }

    /** The virtual user ID that owns system-seeded (built-in) categories. */
    public static final String SYSTEM_USER_ID = "SYSTEM";

    /** UTC offset — use instead of {@code ZoneId.of("UTC")} for consistency. */
    public static final ZoneOffset UTC = ZoneOffset.UTC;
}
