-- Decouple Budget entity from Category entity association while preserving DB-level integrity.
-- This migration is idempotent and safe for existing databases with live data.

SET @db_name = DATABASE();

-- 1) Ensure budgets.category_id column exists.
SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'budgets'
      AND COLUMN_NAME = 'category_id'
);
SET @sql = IF(
    @col_exists = 0,
    'ALTER TABLE budgets ADD COLUMN category_id BINARY(16) NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) Ensure an index exists for category_id lookups.
SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'budgets'
      AND INDEX_NAME = 'idx_budgets_category_id'
);
SET @sql = IF(
    @idx_exists = 0,
    'CREATE INDEX idx_budgets_category_id ON budgets(category_id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3) Ensure FK budgets.category_id -> categories.id exists.
SET @fk_exists = (
    SELECT COUNT(*)
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = @db_name
      AND TABLE_NAME = 'budgets'
      AND COLUMN_NAME = 'category_id'
      AND REFERENCED_TABLE_NAME = 'categories'
      AND REFERENCED_COLUMN_NAME = 'id'
);
SET @sql = IF(
    @fk_exists = 0,
    'ALTER TABLE budgets ADD CONSTRAINT fk_budgets_category_id FOREIGN KEY (category_id) REFERENCES categories(id)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
