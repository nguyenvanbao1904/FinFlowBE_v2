package com.finflow.backend.finance.wealth.application.usecase;

import com.finflow.backend.finance.wealth.application.port.in.WealthSeedAccountTypesPort;
import com.finflow.backend.finance.wealth.application.query.WealthSeedAccountTypesQuery;

import com.finflow.backend.finance.wealth.domain.entity.WealthAccountType;
import com.finflow.backend.finance.wealth.domain.repository.WealthAccountTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WealthSeedAccountTypesUseCase implements WealthSeedAccountTypesPort {

    private final WealthAccountTypeRepository wealthAccountTypeRepository;

    private static final List<WealthAccountType> DEFAULT_TYPES = List.of(
            WealthAccountType.builder().code("CASH").displayName("Tiền mặt").icon("banknote.fill").color("#10B981").isTransactionEligible(true).build(),
            WealthAccountType.builder().code("BANK_ACCOUNT").displayName("Tài khoản ngân hàng").icon("building.columns.fill").color("#3B82F6").isTransactionEligible(true).build(),
            WealthAccountType.builder().code("CREDIT_CARD").displayName("Thẻ tín dụng").icon("creditcard.fill").color("#F97316").isTransactionEligible(true).isDebt(true).build(),
            WealthAccountType.builder().code("BROKERAGE").displayName("Tài khoản chứng khoán").icon("chart.line.uptrend.xyaxis").color("#8B5CF6").isTransactionEligible(true).build(),
            WealthAccountType.builder().code("SAVING_BOOK").displayName("Sổ tiết kiệm").icon("book.closed.fill").color("#10B981").isTransactionEligible(false).build(),
            WealthAccountType.builder().code("REAL_ESTATE").displayName("Bất động sản").icon("house.fill").color("#3B82F6").isTransactionEligible(false).build(),
            WealthAccountType.builder().code("VEHICLE").displayName("Phương tiện").icon("car.fill").color("#8B5CF6").isTransactionEligible(false).build(),
            WealthAccountType.builder().code("STOCK").displayName("Cổ phiếu").icon("chart.line.uptrend.xyaxis").color("#F59E0B").isTransactionEligible(false).build(),
            WealthAccountType.builder().code("CRYPTO").displayName("Tiền kỹ thuật số").icon("bitcoinsign.circle.fill").color("#F97316").isTransactionEligible(false).build(),
            WealthAccountType.builder().code("GOLD").displayName("Vàng").icon("medal.fill").color("#EAB308").isTransactionEligible(false).build(),
            WealthAccountType.builder().code("LOAN").displayName("Khoản vay").icon("doc.text.fill").color("#EF4444").isTransactionEligible(false).isDebt(true).build()
    );

    @Transactional
    @Override
    public void execute(WealthSeedAccountTypesQuery request) {
        if (wealthAccountTypeRepository.count() > 0) {
            log.debug("Wealth account types already seeded, skipping");
            return;
        }
        log.info("Seeding default wealth account types");
        wealthAccountTypeRepository.saveAll(DEFAULT_TYPES);
    }
}
