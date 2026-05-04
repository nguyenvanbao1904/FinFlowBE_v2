package com.finflow.backend.investment.portfolio.application.usecase;

import com.finflow.backend.investment.portfolio.application.dto.TradeTransactionOutput;
import com.finflow.backend.investment.portfolio.application.port.in.GetTradeTransactionsPort;
import com.finflow.backend.investment.portfolio.application.query.GetTradeTransactionsQuery;
import com.finflow.backend.investment.portfolio.domain.repository.TradeTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetTradeTransactionsUseCase implements GetTradeTransactionsPort {

    private final TradeTransactionRepository tradeTransactionRepository;

    @Transactional(readOnly = true)
    @Override
    public Page<TradeTransactionOutput> execute(GetTradeTransactionsQuery query) {
        log.info("Getting trade transactions | userId={} portfolioId={} page={} size={}",
            query.userId(), query.portfolioId(), query.page(), query.size());
        var pageable = PageRequest.of(query.page(), query.size());
        return tradeTransactionRepository
            .findByPortfolio_IdAndPortfolio_UserIdOrderByTransactionDateDesc(
                query.portfolioId(), query.userId(), pageable)
            .map(tx -> TradeTransactionOutput.builder()
                .id(tx.getId())
                .tradeType(tx.getTradeType())
                .symbol(tx.getSymbol())
                .quantity(tx.getQuantity())
                .price(tx.getPrice())
                .totalAmount(tx.getTotalAmount())
                .feeAmount(tx.getFeeAmount())
                .taxAmount(tx.getTaxAmount())
                .transactionDate(tx.getTransactionDate())
                .build());
    }
}
