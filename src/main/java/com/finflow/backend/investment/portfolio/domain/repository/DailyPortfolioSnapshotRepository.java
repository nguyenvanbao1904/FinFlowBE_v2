package com.finflow.backend.investment.portfolio.domain.repository;

import com.finflow.backend.investment.portfolio.domain.entity.DailyPortfolioSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DailyPortfolioSnapshotRepository extends JpaRepository<DailyPortfolioSnapshot, UUID> {

    Optional<DailyPortfolioSnapshot> findByPortfolioIdAndSnapshotDate(UUID portfolioId, LocalDate snapshotDate);

    List<DailyPortfolioSnapshot> findByPortfolioIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(
            UUID portfolioId,
            LocalDate startInclusive,
            LocalDate endInclusive
    );

    boolean existsByPortfolioIdAndSnapshotDate(UUID portfolioId, LocalDate snapshotDate);
}
