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
import java.util.stream.Collectors;

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
        
        log.info("Deleting old non-bank balance sheets for symbol: {}", companyId);
        repository.deleteByCompanyId(companyId);

        List<NonBankBalanceSheet> entities = requestList.stream()
                .map(mapper::toEntity)
                .collect(Collectors.toList());

        log.info("Inserting {} new non-bank balance sheets for symbol: {}", entities.size(), companyId);
        repository.saveAll(entities);
    }
}
