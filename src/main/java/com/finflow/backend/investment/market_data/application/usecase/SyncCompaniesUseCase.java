package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.mapper.InvestmentDataMapper;
import com.finflow.backend.investment.market_data.domain.entity.Company;
import com.finflow.backend.investment.market_data.domain.repository.CompanyRepository;
import com.finflow.backend.investment.market_data.presentation.request.CompanyRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncCompaniesUseCase {

    private final CompanyRepository companyRepository;
    private final InvestmentDataMapper mapper;

    @Transactional
    public void execute(List<CompanyRequestDTO> requests) {
        log.info("Syncing {} companies data...", requests.size());
        // Since id is string (symbol), we can just saveAll which will do an UPSERT (insert or update)
        // No need to delete first because company symbols don't duplicate per company.
        List<Company> companies = requests.stream()
                .map(mapper::toEntity)
                .toList();
        companyRepository.saveAll(companies);
        log.info("Successfully synced {} companies", companies.size());
    }
}
