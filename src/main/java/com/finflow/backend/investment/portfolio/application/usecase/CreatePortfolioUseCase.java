package com.finflow.backend.investment.portfolio.application.usecase;

import com.finflow.backend.investment.portfolio.application.port.in.CreatePortfolioPort;


import com.finflow.backend.investment.portfolio.domain.entity.Portfolio;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioRepository;
import com.finflow.backend.investment.portfolio.application.command.CreatePortfolioCommand;
import com.finflow.backend.investment.portfolio.application.dto.PortfolioResponseOutput;
import com.finflow.backend.finance.wealth.api.WealthAccountApi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreatePortfolioUseCase implements CreatePortfolioPort {

    private final PortfolioRepository portfolioRepository;
    private final WealthAccountApi wealthAccountApi;

    @Transactional
    @Override
    public PortfolioResponseOutput execute(CreatePortfolioCommand command) {
        String userId = command.userId();
        String trimmedName = command.name().trim();
        log.info("Creating portfolio for user: {}", userId);
        UUID wealthAccountId = resolveWealthAccountId(userId, trimmedName, command.wealthAccountId());

        Portfolio portfolio = Portfolio.builder()
                .userId(userId)
                .name(trimmedName)
                .wealthAccountId(wealthAccountId)
                .cashBalance(BigDecimal.ZERO)
                .build();

        Portfolio saved = portfolioRepository.save(portfolio);
        return new PortfolioResponseOutput(
                saved.getId(),
                saved.getName(),
                saved.getWealthAccountId(),
                saved.getCashBalance(),
                null,
                saved.getCashBalance(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }

    private UUID resolveWealthAccountId(String userId, String portfolioName, UUID requestedAccountId) {
        if (requestedAccountId != null) {
            wealthAccountApi.requireBrokerageAccount(userId, requestedAccountId);
            return requestedAccountId;
        }
        String accountName = "Tài khoản chứng khoán - " + portfolioName;
        return wealthAccountApi.createBrokerageAccount(userId, accountName).id();
    }
}
