package com.finflow.backend.investment.portfolio.application.usecase;

import com.finflow.backend.investment.portfolio.application.port.in.CreateTradeTransactionPort;
import com.finflow.backend.investment.portfolio.application.usecase.trade.BuyTradeHandler;
import com.finflow.backend.investment.portfolio.application.usecase.trade.DepositTradeHandler;
import com.finflow.backend.investment.portfolio.application.usecase.trade.DividendTradeHandler;
import com.finflow.backend.investment.portfolio.application.usecase.trade.SellTradeHandler;
import com.finflow.backend.investment.portfolio.application.usecase.trade.TradeContext;
import com.finflow.backend.investment.portfolio.application.usecase.trade.TradeHandler;
import com.finflow.backend.investment.portfolio.application.usecase.trade.WithdrawTradeHandler;
import com.finflow.backend.investment.portfolio.application.service.PortfolioWealthSyncService;
import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.investment.portfolio.domain.entity.Portfolio;
import com.finflow.backend.investment.portfolio.domain.entity.TradeType;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioRepository;
import com.finflow.backend.investment.portfolio.application.command.CreateTradeTransactionCommand;
import com.finflow.backend.investment.portfolio.exception.PortfolioErrorCode;
import com.finflow.backend.investment.portfolio.exception.TradeTransactionErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreateTradeTransactionUseCase implements CreateTradeTransactionPort {

    private final PortfolioRepository portfolioRepository;
    private final BuyTradeHandler buyTradeHandler;
    private final SellTradeHandler sellTradeHandler;
    private final DepositTradeHandler depositTradeHandler;
    private final WithdrawTradeHandler withdrawTradeHandler;
    private final DividendTradeHandler dividendTradeHandler;
    private final PortfolioWealthSyncService portfolioWealthSyncService;

    private Map<TradeType, TradeHandler> handlerMap;

    @PostConstruct
    void initHandlers() {
        handlerMap = new EnumMap<>(TradeType.class);
        handlerMap.put(TradeType.BUY, buyTradeHandler);
        handlerMap.put(TradeType.SELL, sellTradeHandler);
        handlerMap.put(TradeType.DEPOSIT, depositTradeHandler);
        handlerMap.put(TradeType.WITHDRAW, withdrawTradeHandler);
        handlerMap.put(TradeType.DIVIDEND, dividendTradeHandler);
    }

    @Transactional
    @Override
    public void execute(CreateTradeTransactionCommand command) {
        if (command.tradeType() == null || command.tradeType().isBlank()) {
            throw new AppException(TradeTransactionErrorCode.TRADE_TYPE_REQUIRED);
        }
        TradeType tradeType;
        try {
            tradeType = TradeType.valueOf(command.tradeType());
        } catch (IllegalArgumentException e) {
            throw new AppException(TradeTransactionErrorCode.TRADE_TYPE_REQUIRED);
        }

        Portfolio portfolio = portfolioRepository
                .findByIdAndUserId(command.portfolioId(), command.userId())
                .orElseThrow(() -> new AppException(PortfolioErrorCode.PORTFOLIO_NOT_FOUND));

        BigDecimal feePercent = defaultIfNull(command.feePercent(), BigDecimal.ZERO)
                .setScale(6, RoundingMode.HALF_UP);
        BigDecimal taxPercent = defaultIfNull(
                command.taxPercent(),
                tradeType == TradeType.SELL ? new BigDecimal("0.1") : BigDecimal.ZERO)
                .setScale(6, RoundingMode.HALF_UP);

        TradeHandler handler = handlerMap.get(tradeType);
        if (handler == null) {
            throw new AppException(TradeTransactionErrorCode.TRADE_TYPE_REQUIRED);
        }

        handler.handle(new TradeContext(command, portfolio, feePercent, taxPercent));
        portfolioWealthSyncService.syncPortfolioValueToWealth(portfolio.getId(), command.userId());
    }

    private static BigDecimal defaultIfNull(BigDecimal v, BigDecimal defaultValue) {
        return v == null ? defaultValue : v;
    }
}
