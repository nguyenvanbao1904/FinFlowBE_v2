package com.finflow.backend.modules.identity.infrastructure.persistence;

import com.finflow.backend.modules.identity.application.usecase.SeedIdentityDataUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class IdentityDataSeeder implements CommandLineRunner {

    private final SeedIdentityDataUseCase seedIdentityDataUseCase;

    @Override
    public void run(String... args) throws Exception {
        log.info("Triggering identity data seeding on startup...");
        seedIdentityDataUseCase.execute();
    }
}

