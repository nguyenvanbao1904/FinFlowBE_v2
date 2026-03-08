package com.finflow.backend.transaction.infrastructure.persistence;

import com.finflow.backend.transaction.application.usecase.SeedTransactionDataUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
public class TransactionDataSeeder implements CommandLineRunner {

    private final SeedTransactionDataUseCase seedTransactionDataUseCase;

    @Override
    public void run(String... args) throws Exception {
        log.info("Triggering transaction data seeding on startup...");
        seedTransactionDataUseCase.execute();
    }
}
