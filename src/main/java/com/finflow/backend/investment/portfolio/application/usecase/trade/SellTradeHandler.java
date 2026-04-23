package com.finflow.backend.investment.portfolio.application.usecase.trade;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.investment.common.util.StockSymbolUtils;
import com.finflow.backend.investment.portfolio.domain.entity.Portfolio;
import com.finflow.backend.investment.portfolio.domain.entity.PortfolioAsset;
import com.finflow.backend.investment.portfolio.domain.entity.TradeTransaction;
import com.finflow.backend.investment.portfolio.domain.entity.TradeType;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioAssetRepository;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioRepository;
import com.finflow.backend.investment.portfolio.domain.repository.TradeTransactionRepository;
import com.finflow.backend.investment.portfolio.exception.TradeTransactionErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SellTradeHandler implements TradeHandler {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioAssetRepository portfolioAssetRepository;
    private final TradeTransactionRepository tradeTransactionRepository;

    @Override
    public void handle(TradeContext ctx) {
        String symbol = StockSymbolUtils.normalizeSymbol(ctx.command().symbol());
        BigDecimal quantity = ctx.command().quantity();
        BigDecimal price = ctx.command().price();

        if (symbol == null) throw new AppException(TradeTransactionErrorCode.TRADE_SYMBOL_REQUIRED);
        if (quantity == null) throw new AppException(TradeTransactionErrorCode.TRADE_QUANTITY_REQUIRED);
        if (quantity.compareTo(BigDecimal.ZERO) <= 0)
            throw new AppException(TradeTransactionErrorCode.INVALID_TRADE_QUANTITY_NON_POSITIVE);
        if (!StockSymbolUtils.isWholeNumber(quantity))
            throw new AppException(TradeTransactionErrorCode.INVALID_TRADE_QUANTITY_MUST_BE_WHOLE_NUMBER);
        if (price == null) throw new AppException(TradeTransactionErrorCode.TRADE_PRICE_REQUIRED);
        if (price.compareTo(BigDecimal.ZERO) < 0)
            throw new AppException(TradeTransactionErrorCode.INVALID_TRADE_PRICE_NEGATIVE);

        BigDecimal totalAmount = quantity.multiply(price).setScale(2, RoundingMode.HALF_UP);
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0)
            throw new AppException(TradeTransactionErrorCode.INVALID_TRADE_AMOUNT_NON_POSITIVE);

        BigDecimal feeAmount = totalAmount.multiply(ctx.feePercent()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal taxAmount = totalAmount.multiply(ctx.taxPercent()).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        UUID portfolioId = ctx.portfolio().getId();
        String userId = ctx.command().userId();

        PortfolioAsset asset = portfolioAssetRepository
                .findByPortfolio_IdAndPortfolio_UserIdAndSymbol(portfolioId, userId, symbol)
                .orElseThrow(() -> new AppException(TradeTransactionErrorCode.PORTFOLIO_ASSET_QUANTITY_INSUFFICIENT));

        if (asset.getTotalQuantity().compareTo(quantity) < 0)
            throw new AppException(TradeTransactionErrorCode.PORTFOLIO_ASSET_QUANTITY_INSUFFICIENT);

        Portfolio portfolio = ctx.portfolio();
        BigDecimal cashDelta = totalAmount.subtract(feeAmount).subtract(taxAmount);
        portfolio.setCashBalance(TradeHandlerUtils.toScale2(portfolio.getCashBalance().add(cashDelta)));
        portfolioRepository.save(portfolio);

        BigDecimal newQty = asset.getTotalQuantity().subtract(quantity).setScale(0, RoundingMode.HALF_UP);
        if (newQty.compareTo(BigDecimal.ZERO) <= 0) {
            // Keep snapshot rule: quantity==0 -> delete asset row
            portfolioAssetRepository.delete(asset);
        } else {
            asset.setTotalQuantity(newQty);
            // averagePrice does NOT change on SELL
            portfolioAssetRepository.save(asset);
        }

        tradeTransactionRepository.save(TradeTransaction.builder()
                .portfolio(portfolio)
                .tradeType(TradeType.SELL)
                .symbol(symbol)
                .quantity(quantity.setScale(0, RoundingMode.HALF_UP))
                .price(price.setScale(2, RoundingMode.HALF_UP))
                .totalAmount(totalAmount)
                .feeAmount(feeAmount)
                .taxAmount(taxAmount)
                .transactionDate(TradeHandlerUtils.parseDateOrNow(ctx.command().transactionDate()))
                .build());
    }
}
