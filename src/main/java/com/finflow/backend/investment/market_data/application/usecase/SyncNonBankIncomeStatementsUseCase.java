package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.mapper.InvestmentDataMapper;
import com.finflow.backend.investment.market_data.domain.entity.NonBankIncomeStatement;
import com.finflow.backend.investment.market_data.domain.repository.NonBankIncomeStatementRepository;
import com.finflow.backend.investment.market_data.presentation.request.NonBankIncomeStatementRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncNonBankIncomeStatementsUseCase {

    private final NonBankIncomeStatementRepository repository;
    private final InvestmentDataMapper mapper;

    @Transactional
    public void execute(List<NonBankIncomeStatementRequestDTO> requestList) {
        if (requestList == null || requestList.isEmpty()) return;

        String companyId = requestList.get(0).companyId();
        
        log.info("Deleting old non-bank income statements for symbol: {}", companyId);
        repository.deleteByCompanyId(companyId);

        List<NonBankIncomeStatement> entities = requestList.stream()
                .map(mapper::toEntity)
                .collect(Collectors.toList());

        log.info("Inserting {} new non-bank income statements for symbol: {}", entities.size(), companyId);
        repository.saveAll(entities);
    }
}
