package com.finflow.backend.finance.transaction.domain.repository;

import com.finflow.backend.finance.transaction.domain.entity.Category;
import com.finflow.backend.finance.transaction.domain.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByUserId(String userId);
    List<Category> findByUserIdAndType(String userId, CategoryType type);
    Optional<Category> findByIdAndUserId(UUID id, String userId);

    @Query("SELECT c FROM Category c WHERE c.id = :id AND (c.userId = :userId OR c.userId = 'SYSTEM')")
    Optional<Category> findByIdAndUserIdOrSystem(@Param("id") UUID id, @Param("userId") String userId);

    @Query("SELECT c FROM Category c WHERE c.userId = :userId OR c.userId = 'SYSTEM'")
    List<Category> findByUserIdOrSystem(@Param("userId") String userId);
}
