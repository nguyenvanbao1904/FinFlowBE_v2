package com.finflow.backend.investment.market_data.domain.repository;

import com.finflow.backend.investment.market_data.domain.entity.CashFlowStatement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CashFlowStatementRepository extends JpaRepository<CashFlowStatement, CashFlowStatement.PK> {
    void deleteByCompanyId(String companyId);
    List<CashFlowStatement> findByCompanyIdOrderByYearAscQuarterAsc(String companyId);
}
