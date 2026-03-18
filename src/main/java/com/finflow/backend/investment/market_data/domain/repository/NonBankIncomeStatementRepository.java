package com.finflow.backend.investment.market_data.domain.repository;

import com.finflow.backend.investment.market_data.domain.entity.NonBankIncomeStatement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NonBankIncomeStatementRepository extends JpaRepository<NonBankIncomeStatement, UUID> {
    void deleteByCompanyId(String companyId);
}
