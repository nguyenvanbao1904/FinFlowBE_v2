-- Dọn dẹp dữ liệu rác trước (nếu có báo cáo tài chính của công ty không tồn tại)
DELETE FROM company_shareholders WHERE company_id NOT IN (SELECT id FROM companies);
DELETE FROM company_dividends WHERE company_id NOT IN (SELECT id FROM companies);
DELETE FROM income_statements WHERE company_id NOT IN (SELECT id FROM companies);
DELETE FROM balance_sheets WHERE company_id NOT IN (SELECT id FROM companies);
DELETE FROM financial_indicators WHERE company_id NOT IN (SELECT id FROM companies);

-- Thêm các ràng buộc Khóa Ngoại (Foreign Key)
ALTER TABLE company_shareholders ADD CONSTRAINT fk_shareholder_company FOREIGN KEY (company_id) REFERENCES companies(id);
ALTER TABLE company_dividends ADD CONSTRAINT fk_dividend_company FOREIGN KEY (company_id) REFERENCES companies(id);
ALTER TABLE income_statements ADD CONSTRAINT fk_income_company FOREIGN KEY (company_id) REFERENCES companies(id);
ALTER TABLE balance_sheets ADD CONSTRAINT fk_balance_company FOREIGN KEY (company_id) REFERENCES companies(id);
ALTER TABLE financial_indicators ADD CONSTRAINT fk_fin_ind_company FOREIGN KEY (company_id) REFERENCES companies(id);
