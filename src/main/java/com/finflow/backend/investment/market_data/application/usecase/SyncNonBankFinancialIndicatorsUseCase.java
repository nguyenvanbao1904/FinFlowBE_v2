package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.mapper.InvestmentDataMapper;
import com.finflow.backend.investment.market_data.application.command.SyncNonBankFinancialIndicatorsCommand;
import com.finflow.backend.investment.market_data.application.port.in.SyncNonBankFinancialIndicatorsPort;
import com.finflow.backend.investment.market_data.domain.entity.NonBankFinancialIndicator;
import com.finflow.backend.investment.market_data.domain.repository.FinancialIndicatorRepository;
import com.finflow.backend.investment.market_data.application.dto.NonBankFinancialIndicatorRequestInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncNonBankFinancialIndicatorsUseCase implements SyncNonBankFinancialIndicatorsPort {

    private final FinancialIndicatorRepository repository;
    private final InvestmentDataMapper mapper;

    @Transactional
    @Override
    public void execute(SyncNonBankFinancialIndicatorsCommand command) {
        List<NonBankFinancialIndicatorRequestInput> requestList = command.request();
        if (requestList == null || requestList.isEmpty()) return;

        String companyId = requestList.get(0).companyId();

        log.info("Deleting old non-bank financial indicators for symbol: {}", companyId);
        repository.deleteByCompanyId(companyId);

        List<NonBankFinancialIndicator> entities = requestList.stream()
                .map(mapper::toNonBankEntity)
                .collect(Collectors.toList());

        log.info("Inserting {} new non-bank financial indicators for symbol: {}", entities.size(), companyId);
        repository.saveAll(entities);
    }
}
