-- Clarify that the default income category is for realized investment income,
-- not for buying stocks or funding a brokerage account.

UPDATE categories
SET name = 'Thu nhập đầu tư'
WHERE name = 'Đầu tư'
  AND type = 'INCOME'
  AND is_system = 1;
