package com.finflow.backend.investment.market_data.domain.repository;

import com.finflow.backend.investment.market_data.domain.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, String> {
    Optional<Company> findByIdIgnoreCase(String id);

    List<Company> findByIdStartingWithIgnoreCaseOrderByIdAsc(String prefix, Pageable pageable);

    List<Company> findByCompanyNameContainingIgnoreCaseOrderByIdAsc(String keyword, Pageable pageable);

    @Query("""
            select c
            from Company c
            left join fetch c.industryNode node
            where upper(c.id) in :symbolsUpper
            """)
    List<Company> findByIdInUppercase(@Param("symbolsUpper") List<String> symbolsUpper);
}
