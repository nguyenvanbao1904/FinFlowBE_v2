package com.finflow.backend.investment.market_data.domain.repository;

import com.finflow.backend.investment.market_data.domain.entity.NonBankIncomeStatement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NonBankIncomeStatementRepository extends JpaRepository<NonBankIncomeStatement, UUID> {
    void deleteByCompanyId(String companyId);
    List<NonBankIncomeStatement> findByCompanyIdOrderByYearAscQuarterAsc(String companyId);

    List<NonBankIncomeStatement> findByCompanyIdOrderByYearDescQuarterDesc(String companyId, Pageable pageable);
}
