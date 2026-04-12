package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.mapper.InvestmentDataMapper;
import com.finflow.backend.investment.market_data.application.port.in.SyncBankBalanceSheetsPort;
import com.finflow.backend.investment.market_data.domain.entity.BankBalanceSheet;
import com.finflow.backend.investment.market_data.domain.repository.BankBalanceSheetRepository;
import com.finflow.backend.investment.market_data.presentation.request.BankBalanceSheetRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncBankBalanceSheetsUseCase implements SyncBankBalanceSheetsPort {

    private final BankBalanceSheetRepository repository;
    private final InvestmentDataMapper mapper;

    @Transactional
    @Override
    public void execute(List<BankBalanceSheetRequestDTO> requestList) {
        if (requestList == null || requestList.isEmpty()) return;

        String companyId = requestList.get(0).companyId();
        
        log.info("Deleting old bank balance sheets for symbol: {}", companyId);
        repository.deleteByCompanyId(companyId);

        List<BankBalanceSheet> entities = requestList.stream()
                .map(mapper::toEntity)
                .collect(Collectors.toList());

        log.info("Inserting {} new bank balance sheets for symbol: {}", entities.size(), companyId);
        repository.saveAll(entities);
    }
}
