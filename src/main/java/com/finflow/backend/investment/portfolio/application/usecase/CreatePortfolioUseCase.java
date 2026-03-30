package com.finflow.backend.investment.portfolio.application.usecase;

import com.finflow.backend.investment.portfolio.application.mapper.PortfolioMapper;
import com.finflow.backend.investment.portfolio.domain.entity.Portfolio;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioRepository;
import com.finflow.backend.investment.portfolio.presentation.request.CreatePortfolioRequest;
import com.finflow.backend.investment.portfolio.presentation.response.PortfolioResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreatePortfolioUseCase {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioMapper portfolioMapper;

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public PortfolioResponse execute(String userId, CreatePortfolioRequest request) {
        String trimmedName = request.getName().trim();
        log.info("Creating portfolio for user: {}", userId);

        Portfolio portfolio = Portfolio.builder()
                .userId(userId)
                .name(trimmedName)
                .cashBalance(BigDecimal.ZERO)
                .build();

        Portfolio saved = portfolioRepository.save(portfolio);
        return portfolioMapper.toPortfolioResponse(saved);
    }
}

