package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.mapper.InvestmentDataMapper;
import com.finflow.backend.investment.market_data.application.command.SyncCompanyDividendsCommand;
import com.finflow.backend.investment.market_data.application.port.in.SyncCompanyDividendsPort;
import com.finflow.backend.investment.market_data.domain.entity.CompanyDividend;
import com.finflow.backend.investment.market_data.domain.repository.CompanyDividendRepository;
import com.finflow.backend.investment.market_data.application.dto.CompanyDividendRequestInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncCompanyDividendsUseCase implements SyncCompanyDividendsPort {

    private final CompanyDividendRepository repository;
    private final InvestmentDataMapper mapper;

    @Transactional
    @Override
    public void execute(SyncCompanyDividendsCommand command) {
        String companyId = command.companyId();
        List<CompanyDividendRequestInput> requests = command.request();
        log.info("Syncing {} dividend events for company {}", requests.size(), companyId);
        
        repository.deleteByCompanyId(companyId);
        
        List<CompanyDividend> entities = requests.stream()
                .map(mapper::toEntity)
                .peek(e -> e.setCompanyId(companyId))
                .toList();

        repository.saveAll(entities);
        log.info("Successfully synced dividend events for company {}", companyId);
    }
}
