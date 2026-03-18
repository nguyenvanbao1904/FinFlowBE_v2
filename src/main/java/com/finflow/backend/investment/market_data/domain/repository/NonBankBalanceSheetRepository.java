package com.finflow.backend.investment.market_data.domain.repository;

import com.finflow.backend.investment.market_data.domain.entity.NonBankBalanceSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NonBankBalanceSheetRepository extends JpaRepository<NonBankBalanceSheet, UUID> {
    void deleteByCompanyId(String companyId);
}
