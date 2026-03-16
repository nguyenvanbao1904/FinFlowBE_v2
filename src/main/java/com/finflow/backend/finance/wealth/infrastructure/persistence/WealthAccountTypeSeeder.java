package com.finflow.backend.finance.wealth.infrastructure.persistence;

import com.finflow.backend.finance.wealth.application.usecase.WealthSeedAccountTypesUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class WealthAccountTypeSeeder implements CommandLineRunner {

    private final WealthSeedAccountTypesUseCase seedWealthAccountTypesUseCase;

    @Override
    public void run(String... args) {
        log.info("Triggering wealth account type seeding on startup...");
        seedWealthAccountTypesUseCase.execute();
    }
}
