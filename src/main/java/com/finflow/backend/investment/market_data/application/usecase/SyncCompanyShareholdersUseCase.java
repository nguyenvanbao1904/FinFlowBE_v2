package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.mapper.InvestmentDataMapper;
import com.finflow.backend.investment.market_data.application.command.SyncCompanyShareholdersCommand;
import com.finflow.backend.investment.market_data.application.port.in.SyncCompanyShareholdersPort;
import com.finflow.backend.investment.market_data.domain.entity.CompanyShareholder;
import com.finflow.backend.investment.market_data.domain.repository.CompanyShareholderRepository;
import com.finflow.backend.investment.market_data.application.dto.CompanyShareholderRequestInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncCompanyShareholdersUseCase implements SyncCompanyShareholdersPort {

    private final CompanyShareholderRepository repository;
    private final InvestmentDataMapper mapper;

    @Transactional
    @Override
    public void execute(SyncCompanyShareholdersCommand command) {
        String companyId = command.companyId();
        List<CompanyShareholderRequestInput> requests = command.request();
        log.info("Syncing {} shareholders for company {}", requests.size(), companyId);

        Map<String, CompanyShareholder> existingByKey = repository.findByCompanyId(companyId)
                .stream()
                .collect(Collectors.toMap(this::naturalKey, Function.identity(), (left, right) -> left));
        
        List<CompanyShareholder> entities = requests.stream()
                .map(mapper::toEntity)
                .peek(entity -> {
                    entity.setCompanyId(companyId);
                    CompanyShareholder existing = existingByKey.get(naturalKey(entity));
                    if (existing != null) {
                        entity.setId(existing.getId());
                    }
                })
                .toList();

        repository.saveAll(entities);
        log.info("Successfully synced shareholders for company {}", companyId);
    }

    private String naturalKey(CompanyShareholder entity) {
        String name = entity.getShareholderName() == null ? "" : entity.getShareholderName().trim().toUpperCase();
        String date = entity.getUpdateDate() == null ? "" : entity.getUpdateDate().toString();
        return name + "|" + date;
    }
}
