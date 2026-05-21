package com.finflow.backend.investment.portfolio.application.service;

import com.finflow.backend.finance.wealth.api.WealthAccountApi;
import com.finflow.backend.investment.portfolio.api.MarketPriceApi;
import com.finflow.backend.investment.portfolio.api.MarketPriceQuote;
import com.finflow.backend.investment.portfolio.domain.entity.Portfolio;
import com.finflow.backend.investment.portfolio.domain.entity.PortfolioAsset;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioAssetRepository;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortfolioWealthSyncService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioAssetRepository portfolioAssetRepository;
    private final MarketPriceApi marketPriceApi;
    private final WealthAccountApi wealthAccountApi;

    public BigDecimal syncPortfolioValueToWealth(Portfolio portfolio) {
        if (portfolio == null) {
            return BigDecimal.ZERO.setScale(2);
        }
        ensureLinkedBrokerageAccount(portfolio);

        List<PortfolioAsset> assets = portfolioAssetRepository
                .findByPortfolio_IdAndPortfolio_UserId(portfolio.getId(), portfolio.getUserId());
        List<String> symbols = assets.stream().map(PortfolioAsset::getSymbol).toList();
        Map<String, MarketPriceQuote> closePrices = marketPriceApi.getClosePrices(symbols);

        BigDecimal stockValue = assets.stream()
                .map(asset -> holdingValue(asset, closePrices.get(asset.getSymbol())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal portfolioValue = portfolio.getCashBalance()
                .add(stockValue)
                .setScale(2, RoundingMode.HALF_UP);

        wealthAccountApi.updateBalance(portfolio.getWealthAccountId(), portfolioValue);
        log.debug(
                "Synced portfolio {} value {} to wealth account {}",
                portfolio.getId(),
                portfolioValue,
                portfolio.getWealthAccountId()
        );
        return portfolioValue;
    }

    public void syncPortfolioValueToWealth(UUID portfolioId, String userId) {
        portfolioRepository.findByIdAndUserId(portfolioId, userId)
                .ifPresent(portfolio -> syncPortfolioValueToWealth(portfolio));
    }

    public BigDecimal ensurePortfolioLinkedAndSynced(Portfolio portfolio) {
        return syncPortfolioValueToWealth(portfolio);
    }

    public void resetLinkedWealthBalance(Portfolio portfolio) {
        if (portfolio == null || portfolio.getWealthAccountId() == null) {
            return;
        }
        wealthAccountApi.updateBalance(portfolio.getWealthAccountId(), BigDecimal.ZERO.setScale(2));
    }

    private void ensureLinkedBrokerageAccount(Portfolio portfolio) {
        if (portfolio.getWealthAccountId() != null) {
            wealthAccountApi.requireBrokerageAccount(portfolio.getUserId(), portfolio.getWealthAccountId());
            return;
        }
        var account = wealthAccountApi.createBrokerageAccount(
                portfolio.getUserId(),
                "Tài khoản chứng khoán - " + portfolio.getName()
        );
        portfolio.setWealthAccountId(account.id());
        portfolioRepository.save(portfolio);
    }

    private BigDecimal holdingValue(PortfolioAsset asset, MarketPriceQuote quote) {
        BigDecimal unitPrice = quote == null
                ? asset.getAveragePrice()
                : BigDecimal.valueOf(quote.priceVnd()).setScale(2, RoundingMode.HALF_UP);
        return asset.getTotalQuantity()
                .multiply(unitPrice)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
