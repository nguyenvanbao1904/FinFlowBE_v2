package com.finflow.backend.investment.market_data.domain.repository;

import com.finflow.backend.investment.market_data.domain.entity.CompanyDividend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CompanyDividendRepository extends JpaRepository<CompanyDividend, UUID> {
    void deleteByCompanyId(String companyId);
}
