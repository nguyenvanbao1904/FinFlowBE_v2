package com.finflow.backend.identity.application.usecase;

import com.finflow.backend.identity.domain.entity.Role;
import com.finflow.backend.identity.domain.entity.User;
import com.finflow.backend.identity.domain.repository.RoleRepository;
import com.finflow.backend.identity.domain.repository.UserRepository;
import com.finflow.backend.identity.domain.constant.IdentityConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.finflow.backend.identity.application.port.in.SeedIdentityDataPort;
import com.finflow.backend.identity.application.port.out.PasswordEncoderPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeedIdentityDataUseCase implements SeedIdentityDataPort {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoderPort passwordEncoder;

    /**
     * Seed default roles and admin user. Intended to be invoked once at startup.
     */
    @Transactional
    @Override
    public void execute() {
        log.info("Starting Identity Data Seeding...");

        if (!roleRepository.existsById(IdentityConstants.ROLE_USER)) {
            roleRepository.save(Role.builder()
                    .name(IdentityConstants.ROLE_USER)
                    .description("Standard User")
                    .build());
            log.info("Seeded default role: ROLE_USER");
        }

        if (!roleRepository.existsById(IdentityConstants.ROLE_ADMIN)) {
            roleRepository.save(Role.builder()
                    .name(IdentityConstants.ROLE_ADMIN)
                    .description("Administrator")
                    .build());
            log.info("Seeded default role: ROLE_ADMIN");
        }

        // Seed default Admin User
        if (!userRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findById(IdentityConstants.ROLE_ADMIN).orElseThrow();
            User admin = User.builder()
                    .username("admin")
                    .email("admin@finflow.com")
                    .password(passwordEncoder.encode("admin123")) // Default password
                    .firstName("Super")
                    .lastName("Admin")
                    .isActive(true)
                    .accountVerified(true)
                    .roles(new HashSet<>(Collections.singletonList(adminRole)))
                    .build();
            userRepository.save(admin);
            log.info("Seeded default admin user: admin / admin123");
        }

        log.info("Identity Data Seeding Completed.");
    }
}

