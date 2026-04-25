package com.finflow.backend.investment.market_data.domain.repository;

import com.finflow.backend.investment.market_data.domain.entity.IndustryNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IndustryNodeRepository extends JpaRepository<IndustryNode, UUID> {
    Optional<IndustryNode> findByIcbCode(String icbCode);
}
