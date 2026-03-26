package com.finflow.backend.investment.market_data.domain.repository;

import com.finflow.backend.investment.market_data.domain.entity.FinancialIndicator;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FinancialIndicatorRepository extends JpaRepository<FinancialIndicator, UUID> {
    void deleteByCompanyId(String companyId);
    List<FinancialIndicator> findByCompanyIdOrderByYearAscQuarterAsc(String companyId);

    /** Mới nhất trước — dùng với {@link Pageable} để áp dụng LIMIT ở DB. */
    List<FinancialIndicator> findByCompanyIdOrderByYearDescQuarterDesc(String companyId, Pageable pageable);

    /** Tải valuations theo năm (trả đủ mọi quarter trong mỗi năm). */
    List<FinancialIndicator> findByCompanyIdAndYearBetweenOrderByYearAscQuarterAsc(
            String companyId,
            int startYear,
            int endYear
    );

    /** Tải valuations theo cùng một năm, giới hạn theo khoảng quarter. */
    List<FinancialIndicator> findByCompanyIdAndYearAndQuarterBetweenOrderByYearAscQuarterAsc(
            String companyId,
            int year,
            int startQuarter,
            int endQuarter
    );
}
