package com.finflow.backend.goal.domain.repository;

import com.finflow.backend.goal.domain.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GoalRepository extends JpaRepository<Goal, UUID> {
}
