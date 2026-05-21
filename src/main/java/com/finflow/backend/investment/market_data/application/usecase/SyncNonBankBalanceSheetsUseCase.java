package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.mapper.InvestmentDataMapper;
import com.finflow.backend.investment.market_data.application.command.SyncNonBankBalanceSheetsCommand;
import com.finflow.backend.investment.market_data.application.port.in.SyncNonBankBalanceSheetsPort;
import com.finflow.backend.investment.market_data.domain.entity.NonBankBalanceSheet;
import com.finflow.backend.investment.market_data.domain.repository.NonBankBalanceSheetRepository;
import com.finflow.backend.investment.market_data.application.dto.NonBankBalanceSheetRequestInput;
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
public class SyncNonBankBalanceSheetsUseCase implements SyncNonBankBalanceSheetsPort {

    private final NonBankBalanceSheetRepository repository;
    private final InvestmentDataMapper mapper;

    @Transactional
    @Override
    public void execute(SyncNonBankBalanceSheetsCommand command) {
        List<NonBankBalanceSheetRequestInput> requestList = command.request();
        if (requestList == null || requestList.isEmpty()) return;

        String companyId = requestList.get(0).companyId();
        boolean singleCompanyPayload = requestList.stream()
                .allMatch(input -> companyId.equals(input.companyId()));
        if (!singleCompanyPayload) {
            throw new IllegalArgumentException("Non-bank balance sheet sync payload must contain a single companyId");
        }

        Map<String, NonBankBalanceSheet> existingByPeriod = repository.findByCompanyIdOrderByYearAscQuarterAsc(companyId)
                .stream()
                .collect(Collectors.toMap(this::periodKey, Function.identity(), (left, right) -> left));

        List<NonBankBalanceSheet> entities = requestList.stream()
                .map(mapper::toEntity)
                .peek(entity -> {
                    NonBankBalanceSheet existing = existingByPeriod.get(periodKey(entity));
                    if (existing != null) {
                        entity.setId(existing.getId());
                    }
                })
                .collect(Collectors.toList());

        log.info("Upserting {} non-bank balance sheets for symbol: {}", entities.size(), companyId);
        repository.saveAll(entities);
    }

    private String periodKey(NonBankBalanceSheet entity) {
        return entity.getYear() + ":" + entity.getQuarter();
    }
}
