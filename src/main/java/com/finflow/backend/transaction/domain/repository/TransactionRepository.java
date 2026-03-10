package com.finflow.backend.transaction.domain.repository;

import com.finflow.backend.transaction.domain.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    
    Page<Transaction> findByUserIdOrderByTransactionDateDescCreatedAtDesc(String userId, Pageable pageable);
    
    Page<Transaction> findByUserIdAndTransactionDateBetweenOrderByTransactionDateDescCreatedAtDesc(
            String userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    List<Transaction> findByUserIdAndTransactionDateBetweenOrderByTransactionDateDescCreatedAtDesc(
            String userId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.userId = :userId AND t.type = 'INCOME'")
    BigDecimal sumIncomeByUserId(@Param("userId") String userId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.userId = :userId AND t.type = 'EXPENSE'")
    BigDecimal sumExpenseByUserId(@Param("userId") String userId);

    @Query("SELECT t FROM Transaction t JOIN t.category c WHERE t.userId = :userId " +
           "AND (LOWER(t.note) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY t.transactionDate DESC, t.createdAt DESC")
    Page<Transaction> searchByUserIdAndKeyword(
            @Param("userId") String userId, 
            @Param("keyword") String keyword, 
            Pageable pageable);

    @Query("SELECT t FROM Transaction t JOIN t.category c WHERE t.userId = :userId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "AND (LOWER(t.note) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY t.transactionDate DESC, t.createdAt DESC")
    Page<Transaction> searchByUserIdAndDateRangeAndKeyword(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("keyword") String keyword,
            Pageable pageable);
}
