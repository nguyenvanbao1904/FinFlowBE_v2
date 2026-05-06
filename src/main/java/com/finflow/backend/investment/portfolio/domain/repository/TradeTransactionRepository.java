package com.finflow.backend.investment.portfolio.domain.repository;

import com.finflow.backend.investment.portfolio.domain.entity.TradeTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface TradeTransactionRepository extends JpaRepository<TradeTransaction, UUID> {

    void deleteByPortfolio_Id(UUID portfolioId);

    Page<TradeTransaction> findByPortfolio_IdAndPortfolio_UserIdOrderByTransactionDateDesc(
            UUID portfolioId, String userId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(t.totalAmount), 0) FROM TradeTransaction t " +
           "WHERE t.portfolio.userId = :userId AND t.tradeType = 'BUY' " +
           "AND t.transactionDate >= :start AND t.transactionDate < :end")
    BigDecimal sumBuyAmountByUserIdBetween(
            @Param("userId") String userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}

