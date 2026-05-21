package com.finflow.backend.investment.portfolio.application.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.investment.portfolio.application.command.DeletePortfolioCommand;
import com.finflow.backend.investment.portfolio.application.port.in.DeletePortfolioPort;
import com.finflow.backend.investment.portfolio.domain.entity.Portfolio;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioAssetRepository;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioRepository;
import com.finflow.backend.investment.portfolio.domain.repository.TradeTransactionRepository;
import com.finflow.backend.investment.portfolio.exception.PortfolioErrorCode;
import com.finflow.backend.investment.portfolio.application.service.PortfolioWealthSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeletePortfolioUseCase implements DeletePortfolioPort {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioAssetRepository portfolioAssetRepository;
    private final TradeTransactionRepository tradeTransactionRepository;
    private final PortfolioWealthSyncService portfolioWealthSyncService;

    @Transactional
    @Override
    public void execute(DeletePortfolioCommand command) {
        String userId = command.userId();
        log.info("Deleting portfolio for user: {}", userId);

        Portfolio portfolio = portfolioRepository
                .findByIdAndUserId(command.portfolioId(), userId)
                .orElseThrow(() -> new AppException(PortfolioErrorCode.PORTFOLIO_NOT_FOUND));

        portfolioWealthSyncService.resetLinkedWealthBalance(portfolio);
        tradeTransactionRepository.deleteByPortfolio_Id(portfolio.getId());
        portfolioAssetRepository.deleteByPortfolio_Id(portfolio.getId());
        portfolioRepository.delete(portfolio);
    }
}
