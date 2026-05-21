package com.finflow.backend.finance.wealth.domain.repository;

import com.finflow.backend.finance.wealth.domain.entity.WealthAccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WealthAccountTypeRepository extends JpaRepository<WealthAccountType, UUID> {

    List<WealthAccountType> findAllByOrderByCodeAsc();

    Optional<WealthAccountType> findByCode(String code);
}
