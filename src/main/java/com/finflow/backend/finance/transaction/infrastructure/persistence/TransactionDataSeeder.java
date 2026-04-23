package com.finflow.backend.finance.transaction.infrastructure.persistence;

import com.finflow.backend.finance.transaction.application.port.in.SeedTransactionDataPort;
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

    private final SeedTransactionDataPort seedTransactionDataPort;

    @Override
    public void run(String... args) throws Exception {
        log.info("Triggering transaction data seeding on startup...");
        seedTransactionDataPort.execute();
    }
}
