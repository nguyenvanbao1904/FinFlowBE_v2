package com.finflow.backend.finance.wealth.domain.repository;

import com.finflow.backend.finance.wealth.domain.entity.WealthAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WealthAccountRepository extends JpaRepository<WealthAccount, UUID> {

    @Query("SELECT a FROM WealthAccount a JOIN FETCH a.wealthAccountType WHERE a.userId = :userId ORDER BY a.createdAt DESC")
    List<WealthAccount> findAllByUserIdWithType(String userId);

    @Query("SELECT a FROM WealthAccount a JOIN FETCH a.wealthAccountType WHERE a.id = :id AND a.userId = :userId")
    Optional<WealthAccount> findByIdAndUserIdWithType(UUID id, String userId);

    Optional<WealthAccount> findByIdAndUserId(UUID id, String userId);
}
