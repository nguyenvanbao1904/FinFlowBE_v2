package com.finflow.backend.investment.portfolio.application.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.investment.portfolio.application.command.UpdatePortfolioCommand;
import com.finflow.backend.investment.portfolio.application.dto.PortfolioResponseOutput;
import com.finflow.backend.investment.portfolio.application.port.in.UpdatePortfolioPort;
import com.finflow.backend.investment.portfolio.domain.entity.Portfolio;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioRepository;
import com.finflow.backend.investment.portfolio.exception.PortfolioErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdatePortfolioUseCase implements UpdatePortfolioPort {

    private final PortfolioRepository portfolioRepository;

    @Transactional
    @Override
    public PortfolioResponseOutput execute(UpdatePortfolioCommand command) {
        String userId = command.userId();
        log.info("Updating portfolio for user: {}", userId);

        Portfolio portfolio = portfolioRepository
                .findByIdAndUserId(command.portfolioId(), userId)
                .orElseThrow(() -> new AppException(PortfolioErrorCode.PORTFOLIO_NOT_FOUND));

        portfolio.setName(command.name().trim());

        Portfolio saved = portfolioRepository.save(portfolio);
        return new PortfolioResponseOutput(
                saved.getId(),
                saved.getName(),
                saved.getWealthAccountId(),
                saved.getCashBalance(),
                null,
                null,
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }
}
