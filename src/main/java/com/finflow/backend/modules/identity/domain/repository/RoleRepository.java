package com.finflow.backend.modules.identity.domain.repository;

import com.finflow.backend.modules.identity.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, String> {

}