package com.finflow.backend.investment.portfolio.application.usecase.trade;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.investment.common.util.StockSymbolUtils;
import com.finflow.backend.investment.portfolio.domain.entity.Portfolio;
import com.finflow.backend.investment.portfolio.domain.entity.TradeTransaction;
import com.finflow.backend.investment.portfolio.domain.entity.TradeType;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioRepository;
import com.finflow.backend.investment.portfolio.domain.repository.TradeTransactionRepository;
import com.finflow.backend.investment.portfolio.exception.TradeTransactionErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class DividendTradeHandler implements TradeHandler {

    private final PortfolioRepository portfolioRepository;
    private final TradeTransactionRepository tradeTransactionRepository;

    @Override
    public void handle(TradeContext ctx) {
        String symbol = StockSymbolUtils.normalizeSymbol(ctx.command().symbol());
        if (symbol == null) throw new AppException(TradeTransactionErrorCode.TRADE_SYMBOL_REQUIRED);

        BigDecimal totalAmount = ctx.command().amount() != null
                ? ctx.command().amount().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2);
        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(TradeTransactionErrorCode.INVALID_TRADE_AMOUNT_NON_POSITIVE);
        }

        Portfolio portfolio = ctx.portfolio();
        portfolio.setCashBalance(TradeHandlerUtils.toScale2(portfolio.getCashBalance().add(totalAmount)));
        portfolioRepository.save(portfolio);

        tradeTransactionRepository.save(TradeTransaction.builder()
                .portfolio(portfolio)
                .tradeType(TradeType.DIVIDEND)
                .symbol(symbol)
                .quantity(ctx.command().quantity())
                .price(ctx.command().price())
                .totalAmount(totalAmount)
                .feeAmount(BigDecimal.ZERO.setScale(2))
                .taxAmount(BigDecimal.ZERO.setScale(2))
                .transactionDate(TradeHandlerUtils.parseDateOrNow(ctx.command().transactionDate()))
                .build());
    }
}
