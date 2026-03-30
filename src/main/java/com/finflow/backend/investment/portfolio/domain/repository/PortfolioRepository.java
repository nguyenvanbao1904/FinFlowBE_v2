package com.finflow.backend.investment.portfolio.domain.repository;

import com.finflow.backend.investment.portfolio.domain.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PortfolioRepository extends JpaRepository<Portfolio, UUID> {
    List<Portfolio> findByUserIdOrderByCreatedAtDesc(String userId);

    java.util.Optional<Portfolio> findByIdAndUserId(UUID id, String userId);
}

