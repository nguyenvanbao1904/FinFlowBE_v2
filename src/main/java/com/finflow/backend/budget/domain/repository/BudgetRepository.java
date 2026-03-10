package com.finflow.backend.budget.domain.repository;

import com.finflow.backend.budget.domain.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {
}
