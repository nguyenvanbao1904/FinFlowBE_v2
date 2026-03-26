package com.finflow.backend.investment.market_data.domain.repository;

import com.finflow.backend.investment.market_data.domain.entity.CompanyShareholder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompanyShareholderRepository extends JpaRepository<CompanyShareholder, UUID> {
    void deleteByCompanyId(String companyId);
    List<CompanyShareholder> findByCompanyIdOrderByShareOwnPercentDesc(String companyId);
}
