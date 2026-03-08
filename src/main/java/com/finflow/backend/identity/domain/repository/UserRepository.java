package com.finflow.backend.identity.domain.repository;

import com.finflow.backend.identity.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    // For Cleanup Scheduler: Find users deleted before a certain date
    // Query users where deletedAt is NOT NULL and < cutoffDate
    java.util.List<User> findByDeletedAtBefore(java.time.LocalDateTime cutoffDate);
}
