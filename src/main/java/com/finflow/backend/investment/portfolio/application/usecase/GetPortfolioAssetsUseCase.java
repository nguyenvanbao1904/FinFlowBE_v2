package com.finflow.backend.investment.portfolio.application.usecase;

import com.finflow.backend.investment.portfolio.application.mapper.PortfolioAssetMapper;
import com.finflow.backend.investment.portfolio.domain.entity.PortfolioAsset;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioAssetRepository;
import com.finflow.backend.investment.portfolio.infrastructure.VpsMarketPriceClient;
import com.finflow.backend.investment.portfolio.infrastructure.VpsMarketPriceClient.MarketPriceQuote;
import com.finflow.backend.investment.portfolio.presentation.response.PortfolioAssetResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetPortfolioAssetsUseCase {

    private final PortfolioAssetRepository portfolioAssetRepository;
    private final PortfolioAssetMapper portfolioAssetMapper;
    private final VpsMarketPriceClient vpsMarketPriceClient;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<PortfolioAssetResponse> execute(String userId, java.util.UUID portfolioId) {
        log.info("Getting portfolio assets for user: {} portfolioId: {}", userId, portfolioId);
        List<PortfolioAsset> assets = portfolioAssetRepository
                .findByPortfolio_IdAndPortfolio_UserId(portfolioId, userId);

        List<String> symbols = assets.stream().map(PortfolioAsset::getSymbol).toList();
        Map<String, MarketPriceQuote> closePrices = vpsMarketPriceClient.getClosePrices(symbols);

        return assets.stream().map(a -> {
            PortfolioAssetResponse.PortfolioAssetResponseBuilder b = PortfolioAssetResponse.builder()
                    .symbol(a.getSymbol())
                    .totalQuantity(a.getTotalQuantity())
                    .averagePrice(a.getAveragePrice())
                    .updatedAt(a.getUpdatedAt());

            MarketPriceQuote quote = closePrices.get(a.getSymbol());
            if (quote != null) {
                BigDecimal close = BigDecimal.valueOf(quote.priceVnd()).setScale(2, RoundingMode.HALF_UP);
                b.closePrice(close);

                BigDecimal qty = a.getTotalQuantity();
                BigDecimal avg = a.getAveragePrice();
                BigDecimal marketValue = close.multiply(qty).setScale(2, RoundingMode.HALF_UP);
                b.marketValueClose(marketValue);

                BigDecimal pnl = close.subtract(avg).multiply(qty).setScale(2, RoundingMode.HALF_UP);
                b.unrealizedPnL(pnl);

                if (avg.compareTo(BigDecimal.ZERO) != 0) {
                    BigDecimal pnlPct = close
                            .divide(avg, 8, RoundingMode.HALF_UP)
                            .subtract(BigDecimal.ONE)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP);
                    b.unrealizedPnLPct(pnlPct);
                }
            }
            return b.build();
        }).toList();
    }
}

