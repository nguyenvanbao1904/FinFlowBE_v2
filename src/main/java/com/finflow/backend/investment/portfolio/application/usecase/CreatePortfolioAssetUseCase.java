package com.finflow.backend.investment.portfolio.application.usecase;

import com.finflow.backend.investment.portfolio.application.port.in.CreatePortfolioAssetPort;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.investment.common.util.StockSymbolUtils;

import com.finflow.backend.investment.portfolio.domain.entity.Portfolio;
import com.finflow.backend.investment.portfolio.domain.entity.PortfolioAsset;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioAssetRepository;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioRepository;
import com.finflow.backend.investment.portfolio.exception.PortfolioAssetErrorCode;
import com.finflow.backend.investment.portfolio.exception.PortfolioErrorCode;
import com.finflow.backend.investment.portfolio.application.command.CreatePortfolioAssetCommand;
import com.finflow.backend.investment.portfolio.application.dto.PortfolioAssetOutput;
import com.finflow.backend.investment.portfolio.application.service.PortfolioWealthSyncService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreatePortfolioAssetUseCase implements CreatePortfolioAssetPort {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioAssetRepository portfolioAssetRepository;
    private final PortfolioWealthSyncService portfolioWealthSyncService;
    

    @Transactional
    @Override
    public PortfolioAssetOutput execute(CreatePortfolioAssetCommand command) {
        String userId = command.userId();
        UUID portfolioId = command.portfolioId();
        String symbol = command.symbol().trim().toUpperCase();
        BigDecimal quantity = command.quantity();
        BigDecimal averagePrice = command.averagePrice();

        if (symbol.isBlank()) {
            throw new AppException(PortfolioAssetErrorCode.PORTFOLIO_ASSET_SYMBOL_BLANK);
        }
        if (quantity == null) {
            throw new AppException(PortfolioAssetErrorCode.PORTFOLIO_ASSET_QUANTITY_REQUIRED);
        }
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(PortfolioAssetErrorCode.PORTFOLIO_ASSET_QUANTITY_MUST_BE_POSITIVE);
        }
        if (!StockSymbolUtils.isWholeNumber(quantity)) {
            throw new AppException(PortfolioAssetErrorCode.PORTFOLIO_ASSET_QUANTITY_MUST_BE_WHOLE_NUMBER);
        }
        if (averagePrice == null) {
            throw new AppException(PortfolioAssetErrorCode.PORTFOLIO_ASSET_AVERAGE_PRICE_REQUIRED);
        }
        if (averagePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(PortfolioAssetErrorCode.PORTFOLIO_ASSET_AVERAGE_PRICE_MUST_BE_NON_NEGATIVE);
        }

        Portfolio portfolio = portfolioRepository
                .findByIdAndUserId(portfolioId, userId)
                .orElseThrow(() -> new AppException(PortfolioErrorCode.PORTFOLIO_NOT_FOUND));

        PortfolioAsset asset = portfolioAssetRepository
                .findByPortfolio_IdAndPortfolio_UserIdAndSymbol(portfolioId, userId, symbol)
                .orElse(null);

        if (asset == null) {
            PortfolioAsset created = PortfolioAsset.builder()
                    .portfolio(portfolio)
                    .symbol(symbol)
                    .totalQuantity(quantity.setScale(0, RoundingMode.HALF_UP))
                    .averagePrice(averagePrice)
                    .build();

            PortfolioAsset saved = portfolioAssetRepository.save(created);
            portfolioWealthSyncService.syncPortfolioValueToWealth(portfolio);
            return PortfolioAssetOutput.builder()
                    .symbol(saved.getSymbol())
                    .totalQuantity(saved.getTotalQuantity())
                    .averagePrice(saved.getAveragePrice())
                    .build();
        }

        BigDecimal oldQty = asset.getTotalQuantity();
        BigDecimal oldAvg = asset.getAveragePrice();

        BigDecimal newTotalQty = oldQty.add(quantity).setScale(0, RoundingMode.HALF_UP);
        if (newTotalQty.compareTo(BigDecimal.ZERO) <= 0) {
            // Should not happen since quantity is positive and oldQty should be positive too,
            // but keep safe to avoid division errors.
            throw new AppException(PortfolioAssetErrorCode.PORTFOLIO_ASSET_QUANTITY_MUST_BE_POSITIVE);
        }

        // Weighted average: (oldQty * oldAvg + qty * newPrice) / (oldQty + qty)
        BigDecimal weightedSum = oldQty.multiply(oldAvg).add(quantity.multiply(averagePrice));
        BigDecimal newAvg = weightedSum.divide(newTotalQty, 2, RoundingMode.HALF_UP);

        asset.setTotalQuantity(newTotalQty);
        asset.setAveragePrice(newAvg);

        PortfolioAsset saved = portfolioAssetRepository.save(asset);
        portfolioWealthSyncService.syncPortfolioValueToWealth(portfolio);
        return PortfolioAssetOutput.builder()
                .symbol(saved.getSymbol())
                .totalQuantity(saved.getTotalQuantity())
                .averagePrice(saved.getAveragePrice())
                .build();
    }
}
