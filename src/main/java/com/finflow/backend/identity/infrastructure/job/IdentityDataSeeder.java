package com.finflow.backend.identity.infrastructure.job;

import com.finflow.backend.identity.application.port.in.SeedIdentityDataPort;
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

    private final SeedIdentityDataPort seedIdentityDataPort;

    @Override
    public void run(String... args) {
        log.info("Triggering identity data seeding on startup...");
        seedIdentityDataPort.execute();
    }
}
