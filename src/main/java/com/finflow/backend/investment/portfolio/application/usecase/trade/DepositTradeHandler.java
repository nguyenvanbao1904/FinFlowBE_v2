package com.finflow.backend.investment.portfolio.application.usecase.trade;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.finance.wealth.api.WealthAccountApi;
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
public class DepositTradeHandler implements TradeHandler {

    private static final String BROKERAGE_CODE = "BROKERAGE";

    private final WealthAccountApi wealthAccountApi;
    private final PortfolioRepository portfolioRepository;
    private final TradeTransactionRepository tradeTransactionRepository;

    @Override
    public void handle(TradeContext ctx) {
        BigDecimal amount = ctx.command().amount();
        if (amount == null) throw new AppException(TradeTransactionErrorCode.TRADE_AMOUNT_REQUIRED);
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new AppException(TradeTransactionErrorCode.INVALID_TRADE_AMOUNT_NON_POSITIVE);
        if (ctx.command().sourceAccountId() == null) {
            throw new AppException(TradeTransactionErrorCode.TRANSFER_SOURCE_ACCOUNT_REQUIRED);
        }

        Portfolio portfolio = ctx.portfolio();
        WealthAccountApi.AccountSnapshot sourceAccount = wealthAccountApi
                .findAccountWithType(portfolio.getUserId(), ctx.command().sourceAccountId())
                .orElseThrow(() -> new AppException(TradeTransactionErrorCode.TRANSFER_ACCOUNT_NOT_FOUND));
        if (sourceAccount.debt() || !sourceAccount.transactionEligible() || BROKERAGE_CODE.equals(sourceAccount.typeCode())) {
            throw new AppException(TradeTransactionErrorCode.TRANSFER_ACCOUNT_NOT_ELIGIBLE);
        }
        BigDecimal sourceBalanceAfterTransfer = sourceAccount.balance().subtract(amount);
        if (sourceBalanceAfterTransfer.compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(TradeTransactionErrorCode.TRANSFER_SOURCE_ACCOUNT_BALANCE_INSUFFICIENT);
        }

        wealthAccountApi.updateBalance(
                portfolio.getUserId(),
                sourceAccount.id(),
                TradeHandlerUtils.toScale2(sourceBalanceAfterTransfer)
        );

        portfolio.setCashBalance(TradeHandlerUtils.toScale2(portfolio.getCashBalance().add(amount)));
        portfolioRepository.save(portfolio);

        tradeTransactionRepository.save(TradeTransaction.builder()
                .portfolio(portfolio)
                .tradeType(TradeType.DEPOSIT)
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
