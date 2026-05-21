package com.finflow.backend.investment.market_data.domain.repository;

import com.finflow.backend.investment.market_data.domain.entity.CompanyDividend;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompanyDividendRepository extends JpaRepository<CompanyDividend, UUID> {
    void deleteByCompanyId(String companyId);
    List<CompanyDividend> findByCompanyId(String companyId);
    List<CompanyDividend> findByCompanyIdOrderByRecordDateAsc(String companyId);

    /** Gần nhất trước — dùng với {@link Pageable} để LIMIT ở DB. */
    List<CompanyDividend> findByCompanyIdOrderByRecordDateDesc(String companyId, Pageable pageable);
}
