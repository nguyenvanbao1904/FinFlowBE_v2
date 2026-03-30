package com.finflow.backend.investment.portfolio.domain.repository;

import com.finflow.backend.investment.portfolio.domain.entity.DailyMarketIndexSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DailyMarketIndexSnapshotRepository extends JpaRepository<DailyMarketIndexSnapshot, UUID> {

    Optional<DailyMarketIndexSnapshot> findByCodeAndSnapshotDate(String code, LocalDate snapshotDate);

    List<DailyMarketIndexSnapshot> findByCodeAndSnapshotDateBetweenOrderBySnapshotDateAsc(
            String code,
            LocalDate startInclusive,
            LocalDate endInclusive
    );

    boolean existsByCodeAndSnapshotDate(String code, LocalDate snapshotDate);
}
