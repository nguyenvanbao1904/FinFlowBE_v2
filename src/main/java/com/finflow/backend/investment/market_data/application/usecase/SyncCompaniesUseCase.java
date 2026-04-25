package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.command.SyncCompaniesCommand;
import com.finflow.backend.investment.market_data.application.dto.CompanyRequestInput;
import com.finflow.backend.investment.market_data.application.port.in.SyncCompaniesPort;
import com.finflow.backend.investment.market_data.domain.entity.Company;
import com.finflow.backend.investment.market_data.domain.entity.IndustryNode;
import com.finflow.backend.investment.market_data.domain.repository.CompanyRepository;
import com.finflow.backend.investment.market_data.domain.repository.IndustryNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncCompaniesUseCase implements SyncCompaniesPort {

    private final CompanyRepository companyRepository;
    private final IndustryNodeRepository industryNodeRepository;

    @Transactional
    @Override
    public void execute(SyncCompaniesCommand command) {
        List<CompanyRequestInput> requests = command.request();
        log.info("Syncing {} companies data...", requests.size());

        List<Company> toSave = new ArrayList<>(requests.size());
        for (CompanyRequestInput input : requests) {
            Company company = companyRepository.findById(input.id()).orElse(null);
            if (company == null) {
                company = Company.builder()
                        .id(input.id())
                        .exchange(input.exchange() != null ? input.exchange() : "")
                        .companyType(input.companyType() != null ? input.companyType() : "NON_BANK")
                        .build();
            }

            if (input.exchange() != null) company.setExchange(input.exchange());
            if (input.companyType() != null) company.setCompanyType(input.companyType());
            if (input.companyName() != null) company.setCompanyName(input.companyName());
            if (input.description() != null) company.setDescription(input.description());

            if (input.industryIcbCode() != null && !input.industryIcbCode().isBlank()) {
                industryNodeRepository.findByIcbCode(input.industryIcbCode())
                        .ifPresent(company::setIndustryNode);
            }

            toSave.add(company);
        }
        companyRepository.saveAll(toSave);
        log.info("Successfully synced {} companies", toSave.size());
    }
}
