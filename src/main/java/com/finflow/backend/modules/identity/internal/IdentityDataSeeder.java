package com.finflow.backend.modules.identity.internal;

import com.finflow.backend.modules.identity.domain.entity.Role;
import com.finflow.backend.modules.identity.domain.entity.User;
import com.finflow.backend.modules.identity.domain.repository.RoleRepository;
import com.finflow.backend.modules.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class IdentityDataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting Identity Data Seeding...");

        if (!roleRepository.existsById("ROLE_USER")) {
            roleRepository.save(Role.builder()
                    .name("ROLE_USER")
                    .description("Standard User")
                    .build());
            log.info("Seeded default role: ROLE_USER");
        }

        if (!roleRepository.existsById("ROLE_ADMIN")) {
            roleRepository.save(Role.builder()
                    .name("ROLE_ADMIN")
                    .description("Administrator")
                    .build());
            log.info("Seeded default role: ROLE_ADMIN");
        }

        // Seed default Admin User
        if (!userRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findById("ROLE_ADMIN").orElseThrow();
            User admin = User.builder()
                    .username("admin")
                    .email("admin@finflow.com")
                    .password(passwordEncoder.encode("admin123")) // Default password
                    .firstName("Super")
                    .lastName("Admin")
                    .isActive(true)
                    .accountVerified(true)
                    .roles(new HashSet<>(java.util.Collections.singletonList(adminRole)))
                    .build();
            userRepository.save(admin);
            log.info("Seeded default admin user: admin / admin123");
        }

        log.info("Identity Data Seeding Completed.");
    }
}
