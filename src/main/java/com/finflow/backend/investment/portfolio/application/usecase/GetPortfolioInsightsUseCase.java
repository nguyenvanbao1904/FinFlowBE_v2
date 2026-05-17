package com.finflow.backend.investment.portfolio.application.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.investment.market_data.domain.entity.Company;
import com.finflow.backend.investment.market_data.domain.repository.CompanyRepository;
import com.finflow.backend.investment.portfolio.api.MarketPriceApi;
import com.finflow.backend.investment.portfolio.api.MarketPriceQuote;
import com.finflow.backend.investment.portfolio.application.dto.PortfolioInsightItem;
import com.finflow.backend.investment.portfolio.application.port.in.GetPortfolioInsightsPort;
import com.finflow.backend.investment.portfolio.application.port.out.DataAiPortfolioInsightsPort;
import com.finflow.backend.investment.portfolio.application.query.GetPortfolioInsightsQuery;
import com.finflow.backend.investment.portfolio.domain.entity.Portfolio;
import com.finflow.backend.investment.portfolio.domain.entity.PortfolioAsset;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioAssetRepository;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioRepository;
import com.finflow.backend.investment.portfolio.exception.PortfolioErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetPortfolioInsightsUseCase implements GetPortfolioInsightsPort {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioAssetRepository portfolioAssetRepository;
    private final CompanyRepository companyRepository;
    private final MarketPriceApi marketPriceApi;
    private final DataAiPortfolioInsightsPort dataAiPort;

    @Transactional(readOnly = true)
    @Override
    public List<PortfolioInsightItem> execute(GetPortfolioInsightsQuery query) {
        UUID portfolioUuid = UUID.fromString(query.portfolioId());
        Portfolio portfolio = portfolioRepository.findByIdAndUserId(portfolioUuid, query.userId())
                .orElseThrow(() -> new AppException(PortfolioErrorCode.PORTFOLIO_NOT_FOUND));

        List<PortfolioAsset> assets = portfolioAssetRepository.findByPortfolio_IdAndPortfolio_UserId(
                portfolioUuid, query.userId());

        if (assets.isEmpty()) {
            log.debug("Portfolio {} has no assets, skipping insights", query.portfolioId());
            return List.of();
        }

        List<String> symbols = assets.stream().map(PortfolioAsset::getSymbol).toList();
        Map<String, MarketPriceQuote> closePrices = marketPriceApi.getClosePrices(symbols);
        Map<String, String> industryNames = fetchIndustryNames(symbols);

        Map<String, Object> payload = buildPayload(portfolio, assets, closePrices, industryNames);
        return dataAiPort.fetchInsights(payload);
    }

    private Map<String, String> fetchIndustryNames(List<String> symbols) {
        List<String> symbolsUpper = symbols.stream().map(String::toUpperCase).toList();
        List<Company> companies = companyRepository.findByIdInUppercase(symbolsUpper);
        return companies.stream()
                .filter(c -> c.getIndustryNode() != null)
                .collect(Collectors.toMap(
                        c -> c.getId().toUpperCase(),
                        c -> c.getIndustryNode().getNameVi(),
                        (a, b) -> a
                ));
    }

    private Map<String, Object> buildPayload(
            Portfolio portfolio,
            List<PortfolioAsset> assets,
            Map<String, MarketPriceQuote> closePrices,
            Map<String, String> industryNames
    ) {
        BigDecimal totalMarketValue = BigDecimal.ZERO;
        BigDecimal totalCostBasis = BigDecimal.ZERO;
        BigDecimal totalUnrealizedPnL = BigDecimal.ZERO;

        List<Map<String, Object>> assetList = new java.util.ArrayList<>();
        for (PortfolioAsset asset : assets) {
            BigDecimal avg = asset.getAveragePrice();
            BigDecimal qty = asset.getTotalQuantity();
            BigDecimal costBasis = avg.multiply(qty);

            MarketPriceQuote quote = closePrices.get(asset.getSymbol());
            BigDecimal closePrice = quote != null
                    ? BigDecimal.valueOf(quote.priceVnd()).setScale(2, RoundingMode.HALF_UP)
                    : avg;
            BigDecimal marketValue = closePrice.multiply(qty).setScale(2, RoundingMode.HALF_UP);
            BigDecimal unrealizedPnL = closePrice.subtract(avg).multiply(qty).setScale(2, RoundingMode.HALF_UP);
            BigDecimal unrealizedPnLPct = avg.compareTo(BigDecimal.ZERO) != 0
                    ? closePrice.divide(avg, 8, RoundingMode.HALF_UP)
                            .subtract(BigDecimal.ONE)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            totalMarketValue = totalMarketValue.add(marketValue);
            totalCostBasis = totalCostBasis.add(costBasis);
            totalUnrealizedPnL = totalUnrealizedPnL.add(unrealizedPnL);

            Map<String, Object> assetMap = new HashMap<>();
            assetMap.put("symbol", asset.getSymbol());
            assetMap.put("quantity", qty);
            assetMap.put("averagePrice", avg);
            assetMap.put("currentPrice", closePrice);
            assetMap.put("marketValue", marketValue);
            assetMap.put("unrealizedPnL", unrealizedPnL);
            assetMap.put("unrealizedPnLPct", unrealizedPnLPct);
            assetMap.put("industryName", industryNames.get(asset.getSymbol().toUpperCase()));
            assetList.add(assetMap);
        }

        BigDecimal unrealizedPnLPct = totalCostBasis.compareTo(BigDecimal.ZERO) > 0
                ? totalUnrealizedPnL.divide(totalCostBasis, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        Map<String, Object> payload = new HashMap<>();
        payload.put("portfolioName", portfolio.getName());
        payload.put("totalMarketValue", totalMarketValue);
        payload.put("totalCostBasis", totalCostBasis);
        payload.put("cashBalance", portfolio.getCashBalance());
        payload.put("unrealizedPnL", totalUnrealizedPnL);
        payload.put("unrealizedPnLPct", unrealizedPnLPct);
        payload.put("assets", assetList);

        return payload;
    }
}
