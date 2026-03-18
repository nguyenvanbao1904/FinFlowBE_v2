package com.finflow.backend.investment.market_data.domain.repository;

import com.finflow.backend.investment.market_data.domain.entity.BankBalanceSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BankBalanceSheetRepository extends JpaRepository<BankBalanceSheet, UUID> {
    void deleteByCompanyId(String companyId);
}
