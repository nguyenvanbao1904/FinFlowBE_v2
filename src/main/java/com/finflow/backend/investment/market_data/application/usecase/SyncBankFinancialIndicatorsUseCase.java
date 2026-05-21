package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.mapper.InvestmentDataMapper;
import com.finflow.backend.investment.market_data.application.command.SyncBankFinancialIndicatorsCommand;
import com.finflow.backend.investment.market_data.application.port.in.SyncBankFinancialIndicatorsPort;
import com.finflow.backend.investment.market_data.domain.entity.BankFinancialIndicator;
import com.finflow.backend.investment.market_data.domain.repository.FinancialIndicatorRepository;
import com.finflow.backend.investment.market_data.application.dto.BankFinancialIndicatorRequestInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncBankFinancialIndicatorsUseCase implements SyncBankFinancialIndicatorsPort {

    private final FinancialIndicatorRepository repository;
    private final InvestmentDataMapper mapper;

    @Transactional
    @Override
    public void execute(SyncBankFinancialIndicatorsCommand command) {
        List<BankFinancialIndicatorRequestInput> requestList = command.request();
        if (requestList == null || requestList.isEmpty()) return;

        String companyId = requestList.get(0).companyId();
        boolean singleCompanyPayload = requestList.stream()
                .allMatch(input -> companyId.equals(input.companyId()));
        if (!singleCompanyPayload) {
            throw new IllegalArgumentException("Bank financial indicator sync payload must contain a single companyId");
        }

        Map<String, BankFinancialIndicator> existingByPeriod = repository.findByCompanyIdOrderByYearAscQuarterAsc(companyId)
                .stream()
                .filter(BankFinancialIndicator.class::isInstance)
                .map(BankFinancialIndicator.class::cast)
                .collect(Collectors.toMap(this::periodKey, Function.identity(), (left, right) -> left));

        List<BankFinancialIndicator> entities = requestList.stream()
                .map(mapper::toBankEntity)
                .peek(entity -> {
                    BankFinancialIndicator existing = existingByPeriod.get(periodKey(entity));
                    if (existing != null) {
                        entity.setId(existing.getId());
                    }
                })
                .collect(Collectors.toList());

        log.info("Upserting {} bank financial indicators for symbol: {}", entities.size(), companyId);
        repository.saveAll(entities);
    }

    private String periodKey(BankFinancialIndicator entity) {
        return entity.getYear() + ":" + entity.getQuarter();
    }
}
