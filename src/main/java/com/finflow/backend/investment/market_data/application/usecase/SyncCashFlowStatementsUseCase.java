package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.mapper.InvestmentDataMapper;
import com.finflow.backend.investment.market_data.application.command.SyncCashFlowStatementsCommand;
import com.finflow.backend.investment.market_data.application.port.in.SyncCashFlowStatementsPort;
import com.finflow.backend.investment.market_data.domain.entity.CashFlowStatement;
import com.finflow.backend.investment.market_data.domain.repository.CashFlowStatementRepository;
import com.finflow.backend.investment.market_data.application.dto.CashFlowStatementRequestInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncCashFlowStatementsUseCase implements SyncCashFlowStatementsPort {

    private final CashFlowStatementRepository repository;
    private final InvestmentDataMapper mapper;

    @Transactional
    @Override
    public void execute(SyncCashFlowStatementsCommand command) {
        List<CashFlowStatementRequestInput> requestList = command.request();
        if (requestList == null || requestList.isEmpty()) return;

        String companyId = requestList.get(0).companyId();
        boolean singleCompanyPayload = requestList.stream()
                .allMatch(input -> companyId.equals(input.companyId()));
        if (!singleCompanyPayload) {
            throw new IllegalArgumentException("Cash flow sync payload must contain a single companyId");
        }

        List<CashFlowStatement> entities = requestList.stream()
                .map(mapper::toEntity)
                .collect(Collectors.toList());

        log.info("Upserting {} cash flow statements for symbol: {}", entities.size(), companyId);
        repository.saveAll(entities);
    }
}
