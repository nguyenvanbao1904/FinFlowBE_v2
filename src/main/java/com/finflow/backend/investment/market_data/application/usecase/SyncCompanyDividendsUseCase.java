package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.mapper.InvestmentDataMapper;
import com.finflow.backend.investment.market_data.domain.entity.CompanyDividend;
import com.finflow.backend.investment.market_data.domain.repository.CompanyDividendRepository;
import com.finflow.backend.investment.market_data.presentation.request.CompanyDividendRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncCompanyDividendsUseCase {

    private final CompanyDividendRepository repository;
    private final InvestmentDataMapper mapper;

    @Transactional
    public void execute(String companyId, List<CompanyDividendRequestDTO> requests) {
        log.info("Syncing {} dividend events for company {}", requests.size(), companyId);
        
        repository.deleteByCompanyId(companyId);
        
        List<CompanyDividend> entities = requests.stream()
                .map(mapper::toEntity)
                .toList();

        repository.saveAll(entities);
        log.info("Successfully synced dividend events for company {}", companyId);
    }
}
