package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.mapper.InvestmentDataMapper;
import com.finflow.backend.investment.market_data.application.command.SyncCompaniesCommand;
import com.finflow.backend.investment.market_data.application.port.in.SyncCompaniesPort;
import com.finflow.backend.investment.market_data.domain.entity.Company;
import com.finflow.backend.investment.market_data.domain.repository.CompanyRepository;
import com.finflow.backend.investment.market_data.application.dto.CompanyRequestInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncCompaniesUseCase implements SyncCompaniesPort {

    private final CompanyRepository companyRepository;
    private final InvestmentDataMapper mapper;

    @Transactional
    @Override
    public void execute(SyncCompaniesCommand command) {
        List<CompanyRequestInput> requests = command.request();
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
