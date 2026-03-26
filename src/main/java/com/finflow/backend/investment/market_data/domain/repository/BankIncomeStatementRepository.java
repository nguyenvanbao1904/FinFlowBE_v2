package com.finflow.backend.investment.market_data.domain.repository;

import com.finflow.backend.investment.market_data.domain.entity.BankIncomeStatement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BankIncomeStatementRepository extends JpaRepository<BankIncomeStatement, UUID> {
    void deleteByCompanyId(String companyId);
    List<BankIncomeStatement> findByCompanyIdOrderByYearAscQuarterAsc(String companyId);

    List<BankIncomeStatement> findByCompanyIdOrderByYearDescQuarterDesc(String companyId, Pageable pageable);
}
