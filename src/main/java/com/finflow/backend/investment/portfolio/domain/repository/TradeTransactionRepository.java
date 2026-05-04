package com.finflow.backend.investment.portfolio.domain.repository;

import com.finflow.backend.investment.portfolio.domain.entity.TradeTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TradeTransactionRepository extends JpaRepository<TradeTransaction, UUID> {

    void deleteByPortfolio_Id(UUID portfolioId);

    Page<TradeTransaction> findByPortfolio_IdAndPortfolio_UserIdOrderByTransactionDateDesc(
            UUID portfolioId, String userId, Pageable pageable);
}

