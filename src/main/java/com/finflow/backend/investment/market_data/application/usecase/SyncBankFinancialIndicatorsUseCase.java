package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.mapper.InvestmentDataMapper;
import com.finflow.backend.investment.market_data.domain.entity.BankFinancialIndicator;
import com.finflow.backend.investment.market_data.domain.repository.FinancialIndicatorRepository;
import com.finflow.backend.investment.market_data.presentation.request.BankFinancialIndicatorRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncBankFinancialIndicatorsUseCase {

    private final FinancialIndicatorRepository repository;
    private final InvestmentDataMapper mapper;

    @Transactional
    public void execute(List<BankFinancialIndicatorRequestDTO> requestList) {
        if (requestList == null || requestList.isEmpty()) return;

        String companyId = requestList.get(0).companyId();

        log.info("Deleting old bank financial indicators for symbol: {}", companyId);
        repository.deleteByCompanyId(companyId);

        List<BankFinancialIndicator> entities = requestList.stream()
                .map(mapper::toBankEntity)
                .collect(Collectors.toList());

        log.info("Inserting {} new bank financial indicators for symbol: {}", entities.size(), companyId);
        repository.saveAll(entities);
    }
}
