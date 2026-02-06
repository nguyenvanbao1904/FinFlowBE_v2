package com.finflow.backend.modules.identity.domain.repository;

import com.finflow.backend.modules.identity.domain.entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {
    void deleteByExpiryTimeBefore(Date now);
    long countByExpiryTimeBefore(Date now);
}