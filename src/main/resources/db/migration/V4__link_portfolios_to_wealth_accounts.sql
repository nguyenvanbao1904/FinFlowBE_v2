-- Link manual investment portfolios to wealth brokerage accounts for net-worth aggregation.

SET @db_name = DATABASE();

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'portfolios'
      AND COLUMN_NAME = 'wealth_account_id'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE portfolios ADD COLUMN wealth_account_id BINARY(16) NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'portfolios'
      AND INDEX_NAME = 'idx_portfolios_wealth_account_id'
);
SET @sql = IF(
    @idx_exists = 0,
    'CREATE INDEX idx_portfolios_wealth_account_id ON portfolios(wealth_account_id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
