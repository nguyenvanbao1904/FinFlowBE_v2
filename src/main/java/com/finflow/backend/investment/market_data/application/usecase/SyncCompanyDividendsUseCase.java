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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        Map<String, CompanyDividend> existingByKey = repository.findByCompanyId(companyId)
                .stream()
                .collect(Collectors.toMap(this::naturalKey, Function.identity(), (left, right) -> left));
        
        List<CompanyDividend> entities = requests.stream()
                .map(mapper::toEntity)
                .peek(entity -> {
                    entity.setCompanyId(companyId);
                    CompanyDividend existing = existingByKey.get(naturalKey(entity));
                    if (existing != null) {
                        entity.setId(existing.getId());
                    }
                })
                .toList();

        repository.saveAll(entities);
        log.info("Successfully synced dividend events for company {}", companyId);
    }

    private String naturalKey(CompanyDividend entity) {
        String title = entity.getEventTitle() == null ? "" : entity.getEventTitle().trim().toUpperCase();
        String type = entity.getEventType() == null ? "" : entity.getEventType().trim().toUpperCase();
        String ratio = entity.getRatio() == null ? "" : entity.getRatio().trim().toUpperCase();
        String recordDate = entity.getRecordDate() == null ? "" : entity.getRecordDate().toString();
        String exrightDate = entity.getExrightDate() == null ? "" : entity.getExrightDate().toString();
        String issueDate = entity.getIssueDate() == null ? "" : entity.getIssueDate().toString();
        return title + "|" + type + "|" + ratio + "|" + recordDate + "|" + exrightDate + "|" + issueDate;
    }
}
