package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.mapper.InvestmentDataMapper;
import com.finflow.backend.investment.market_data.application.command.SyncBankBalanceSheetsCommand;
import com.finflow.backend.investment.market_data.application.port.in.SyncBankBalanceSheetsPort;
import com.finflow.backend.investment.market_data.domain.entity.BankBalanceSheet;
import com.finflow.backend.investment.market_data.domain.repository.BankBalanceSheetRepository;
import com.finflow.backend.investment.market_data.application.dto.BankBalanceSheetRequestInput;
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
public class SyncBankBalanceSheetsUseCase implements SyncBankBalanceSheetsPort {

    private final BankBalanceSheetRepository repository;
    private final InvestmentDataMapper mapper;

    @Transactional
    @Override
    public void execute(SyncBankBalanceSheetsCommand command) {
        List<BankBalanceSheetRequestInput> requestList = command.request();
        if (requestList == null || requestList.isEmpty()) return;

        String companyId = requestList.get(0).companyId();
        boolean singleCompanyPayload = requestList.stream()
                .allMatch(input -> companyId.equals(input.companyId()));
        if (!singleCompanyPayload) {
            throw new IllegalArgumentException("Bank balance sheet sync payload must contain a single companyId");
        }

        Map<String, BankBalanceSheet> existingByPeriod = repository.findByCompanyIdOrderByYearAscQuarterAsc(companyId)
                .stream()
                .collect(Collectors.toMap(this::periodKey, Function.identity(), (left, right) -> left));

        List<BankBalanceSheet> entities = requestList.stream()
                .map(mapper::toEntity)
                .peek(entity -> {
                    BankBalanceSheet existing = existingByPeriod.get(periodKey(entity));
                    if (existing != null) {
                        entity.setId(existing.getId());
                    }
                })
                .collect(Collectors.toList());

        log.info("Upserting {} bank balance sheets for symbol: {}", entities.size(), companyId);
        repository.saveAll(entities);
    }

    private String periodKey(BankBalanceSheet entity) {
        return entity.getYear() + ":" + entity.getQuarter();
    }
}
