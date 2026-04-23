package com.finflow.backend.investment.portfolio.application.usecase.trade;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.investment.portfolio.domain.entity.Portfolio;
import com.finflow.backend.investment.portfolio.domain.entity.TradeTransaction;
import com.finflow.backend.investment.portfolio.domain.entity.TradeType;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioRepository;
import com.finflow.backend.investment.portfolio.domain.repository.TradeTransactionRepository;
import com.finflow.backend.investment.portfolio.exception.TradeTransactionErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class WithdrawTradeHandler implements TradeHandler {

    private final PortfolioRepository portfolioRepository;
    private final TradeTransactionRepository tradeTransactionRepository;

    @Override
    public void handle(TradeContext ctx) {
        BigDecimal amount = ctx.command().amount();
        if (amount == null) throw new AppException(TradeTransactionErrorCode.TRADE_AMOUNT_REQUIRED);
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new AppException(TradeTransactionErrorCode.INVALID_TRADE_AMOUNT_NON_POSITIVE);

        Portfolio portfolio = ctx.portfolio();
        if (portfolio.getCashBalance().compareTo(amount) < 0)
            throw new AppException(TradeTransactionErrorCode.PORTFOLIO_CASH_BALANCE_INSUFFICIENT);

        portfolio.setCashBalance(TradeHandlerUtils.toScale2(portfolio.getCashBalance().subtract(amount)));
        portfolioRepository.save(portfolio);

        tradeTransactionRepository.save(TradeTransaction.builder()
                .portfolio(portfolio)
                .tradeType(TradeType.WITHDRAW)
                .symbol(null)
                .quantity(null)
                .price(null)
                .totalAmount(TradeHandlerUtils.toScale2(amount))
                .feeAmount(BigDecimal.ZERO.setScale(2))
                .taxAmount(BigDecimal.ZERO.setScale(2))
                .transactionDate(TradeHandlerUtils.parseDateOrNow(ctx.command().transactionDate()))
                .build());
    }
}
