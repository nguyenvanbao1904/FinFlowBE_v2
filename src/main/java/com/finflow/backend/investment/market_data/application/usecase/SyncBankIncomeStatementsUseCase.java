package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.mapper.InvestmentDataMapper;
import com.finflow.backend.investment.market_data.domain.entity.BankIncomeStatement;
import com.finflow.backend.investment.market_data.domain.repository.BankIncomeStatementRepository;
import com.finflow.backend.investment.market_data.presentation.request.BankIncomeStatementRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncBankIncomeStatementsUseCase {

    private final BankIncomeStatementRepository repository;
    private final InvestmentDataMapper mapper;

    @Transactional
    public void execute(List<BankIncomeStatementRequestDTO> requestList) {
        if (requestList == null || requestList.isEmpty()) return;

        String companyId = requestList.get(0).companyId();
        
        log.info("Deleting old bank income statements for symbol: {}", companyId);
        repository.deleteByCompanyId(companyId);

        List<BankIncomeStatement> entities = requestList.stream()
                .map(mapper::toEntity)
                .collect(Collectors.toList());

        log.info("Inserting {} new bank income statements for symbol: {}", entities.size(), companyId);
        repository.saveAll(entities);
    }
}
