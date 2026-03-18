package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.mapper.InvestmentDataMapper;
import com.finflow.backend.investment.market_data.domain.entity.FinancialIndicator;
import com.finflow.backend.investment.market_data.domain.repository.FinancialIndicatorRepository;
import com.finflow.backend.investment.market_data.presentation.request.FinancialIndicatorRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncFinancialIndicatorsUseCase {

    private final FinancialIndicatorRepository repository;
    private final InvestmentDataMapper mapper;

    @Transactional
    public void execute(List<FinancialIndicatorRequestDTO> requestList) {
        if (requestList == null || requestList.isEmpty()) return;

        // Assuming all DTOs belong to the same symbol (based on Python crawler design)
        String companyId = requestList.get(0).companyId();
        
        log.info("Deleting old financial indicators for symbol: {}", companyId);
        repository.deleteByCompanyId(companyId);

        List<FinancialIndicator> entities = requestList.stream()
                .map(mapper::toEntity)
                .collect(Collectors.toList());

        log.info("Inserting {} new financial indicators for symbol: {}", entities.size(), companyId);
        repository.saveAll(entities);
    }
}
