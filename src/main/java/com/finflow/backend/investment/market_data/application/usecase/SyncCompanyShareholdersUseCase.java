package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.mapper.InvestmentDataMapper;
import com.finflow.backend.investment.market_data.domain.entity.CompanyShareholder;
import com.finflow.backend.investment.market_data.domain.repository.CompanyShareholderRepository;
import com.finflow.backend.investment.market_data.presentation.request.CompanyShareholderRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncCompanyShareholdersUseCase {

    private final CompanyShareholderRepository repository;
    private final InvestmentDataMapper mapper;

    @Transactional
    public void execute(String companyId, List<CompanyShareholderRequestDTO> requests) {
        log.info("Syncing {} shareholders for company {}", requests.size(), companyId);
        
        repository.deleteByCompanyId(companyId);
        
        List<CompanyShareholder> entities = requests.stream()
                .map(mapper::toEntity)
                .toList();

        repository.saveAll(entities);
        log.info("Successfully synced shareholders for company {}", companyId);
    }
}
