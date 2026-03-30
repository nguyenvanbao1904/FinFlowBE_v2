package com.finflow.backend.investment.portfolio.domain.repository;

import com.finflow.backend.investment.portfolio.domain.entity.TradeTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TradeTransactionRepository extends JpaRepository<TradeTransaction, UUID> {
}

