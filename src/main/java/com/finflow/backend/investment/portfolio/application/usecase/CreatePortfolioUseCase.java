package com.finflow.backend.investment.portfolio.application.usecase;

import com.finflow.backend.investment.portfolio.application.port.in.CreatePortfolioPort;


import com.finflow.backend.investment.portfolio.domain.entity.Portfolio;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioRepository;
import com.finflow.backend.investment.portfolio.application.command.CreatePortfolioCommand;
import com.finflow.backend.investment.portfolio.application.dto.PortfolioResponseOutput;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreatePortfolioUseCase implements CreatePortfolioPort {

    private final PortfolioRepository portfolioRepository;

    @Transactional
    @Override
    public PortfolioResponseOutput execute(CreatePortfolioCommand command) {
        String userId = command.userId();
        String trimmedName = command.name().trim();
        log.info("Creating portfolio for user: {}", userId);

        Portfolio portfolio = Portfolio.builder()
                .userId(userId)
                .name(trimmedName)
                .cashBalance(BigDecimal.ZERO)
                .build();

        Portfolio saved = portfolioRepository.save(portfolio);
        return new PortfolioResponseOutput(
                saved.getId(),
                saved.getName(),
                saved.getCashBalance(),
                null,
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }
}

