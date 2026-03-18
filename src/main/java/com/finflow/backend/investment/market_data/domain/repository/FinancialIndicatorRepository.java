package com.finflow.backend.investment.market_data.domain.repository;

import com.finflow.backend.investment.market_data.domain.entity.FinancialIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FinancialIndicatorRepository extends JpaRepository<FinancialIndicator, UUID> {
    void deleteByCompanyId(String companyId);
}
