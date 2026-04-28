-- Fix precision for ratio columns in financial_indicators.
-- FireAnt returns ROE/ROA/margins as decimals (e.g. 0.1751 for 17.51%).
-- Previous scale=2 truncated these to 0.18. Increase to scale=6.

ALTER TABLE financial_indicators
    MODIFY COLUMN roe DECIMAL(20, 6),
    MODIFY COLUMN roa DECIMAL(20, 6),
    MODIFY COLUMN pe  DECIMAL(20, 6),
    MODIFY COLUMN pb  DECIMAL(20, 6),
    MODIFY COLUMN ps  DECIMAL(20, 6),
    MODIFY COLUMN gross_margin DECIMAL(20, 6),
    MODIFY COLUMN net_margin DECIMAL(20, 6);
