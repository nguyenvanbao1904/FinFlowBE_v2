package com.finflow.backend.investment.portfolio.application.usecase;

import com.finflow.backend.investment.portfolio.application.port.in.GetPortfolioAssetsPort;
import com.finflow.backend.investment.portfolio.application.query.GetPortfolioAssetsQuery;

import com.finflow.backend.investment.portfolio.application.dto.PortfolioAssetOutput;
import com.finflow.backend.investment.portfolio.api.MarketPriceQuote;
import com.finflow.backend.investment.portfolio.api.MarketPriceApi;
import com.finflow.backend.investment.portfolio.domain.entity.PortfolioAsset;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioAssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetPortfolioAssetsUseCase implements GetPortfolioAssetsPort {

    private final PortfolioAssetRepository portfolioAssetRepository;
    private final MarketPriceApi marketPriceApi;

    @Transactional(readOnly = true)
    @Override
    public List<PortfolioAssetOutput> execute(GetPortfolioAssetsQuery request) {
        String userId = request.userId();
        java.util.UUID portfolioId = request.portfolioId();
        log.info("Getting portfolio assets for user: {} portfolioId: {}", userId, portfolioId);
        List<PortfolioAsset> assets = portfolioAssetRepository
                .findByPortfolio_IdAndPortfolio_UserId(portfolioId, userId);

        List<String> symbols = assets.stream().map(PortfolioAsset::getSymbol).toList();
        Map<String, MarketPriceQuote> closePrices = marketPriceApi.getClosePrices(symbols);

        return assets.stream().map(a -> {
            PortfolioAssetOutput.PortfolioAssetOutputBuilder b = PortfolioAssetOutput.builder()
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

