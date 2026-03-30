package com.finflow.backend.investment.portfolio.application.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.investment.portfolio.domain.entity.Portfolio;
import com.finflow.backend.investment.portfolio.domain.entity.PortfolioAsset;
import com.finflow.backend.investment.portfolio.domain.entity.TradeTransaction;
import com.finflow.backend.investment.portfolio.domain.entity.TradeType;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioAssetRepository;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioRepository;
import com.finflow.backend.investment.portfolio.domain.repository.TradeTransactionRepository;
import com.finflow.backend.investment.portfolio.exception.ImportPortfolioSnapshotErrorCode;
import com.finflow.backend.investment.portfolio.exception.TradeTransactionErrorCode;
import com.finflow.backend.investment.portfolio.presentation.request.CreateTradeTransactionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateTradeTransactionUseCase {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioAssetRepository portfolioAssetRepository;
    private final TradeTransactionRepository tradeTransactionRepository;

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public void execute(String userId, UUID portfolioId, CreateTradeTransactionRequest request) {
        TradeType tradeType = request.getTradeType();
        if (tradeType == null) {
            throw new AppException(TradeTransactionErrorCode.TRADE_TYPE_REQUIRED);
        }

        Portfolio portfolio = portfolioRepository
                .findByIdAndUserId(portfolioId, userId)
                .orElseThrow(() -> new AppException(com.finflow.backend.investment.portfolio.exception.PortfolioErrorCode.PORTFOLIO_NOT_FOUND));

        LocalDateTime transactionDate = parseDateOrNow(request.getTransactionDate());

        BigDecimal feePercent = defaultIfNull(request.getFeePercent(), BigDecimal.ZERO);
        BigDecimal taxPercent = request.getTaxPercent();
        if (taxPercent == null) {
            taxPercent = tradeType == TradeType.SELL ? new BigDecimal("0.1") : BigDecimal.ZERO;
        }

        // Normalize monetary math to 2 decimals.
        feePercent = feePercent.setScale(6, RoundingMode.HALF_UP);
        taxPercent = taxPercent.setScale(6, RoundingMode.HALF_UP);

        BigDecimal feeAmount;
        BigDecimal taxAmount;
        BigDecimal cashDelta; // positive -> add cash, negative -> subtract cash

        String symbol;
        BigDecimal quantity;
        BigDecimal price;
        BigDecimal amount;

        switch (tradeType) {
            case DEPOSIT -> {
                amount = request.getAmount();
                if (amount == null) throw new AppException(TradeTransactionErrorCode.TRADE_AMOUNT_REQUIRED);
                if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new AppException(TradeTransactionErrorCode.INVALID_TRADE_AMOUNT_NON_POSITIVE);

                cashDelta = amount;
                feeAmount = BigDecimal.ZERO.setScale(2);
                taxAmount = BigDecimal.ZERO.setScale(2);

                portfolio.setCashBalance(toScale2(portfolio.getCashBalance().add(cashDelta)));
                tradeTransactionRepository.save(
                        TradeTransaction.builder()
                                .portfolio(portfolio)
                                .tradeType(tradeType)
                                .symbol(null)
                                .quantity(null)
                                .price(null)
                                .totalAmount(toScale2(amount))
                                .feeAmount(feeAmount)
                                .taxAmount(taxAmount)
                                .transactionDate(transactionDate)
                                .build()
                );
            }
            case WITHDRAW -> {
                amount = request.getAmount();
                if (amount == null) throw new AppException(TradeTransactionErrorCode.TRADE_AMOUNT_REQUIRED);
                if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new AppException(TradeTransactionErrorCode.INVALID_TRADE_AMOUNT_NON_POSITIVE);

                if (portfolio.getCashBalance().compareTo(amount) < 0) {
                    throw new AppException(TradeTransactionErrorCode.PORTFOLIO_CASH_BALANCE_INSUFFICIENT);
                }

                cashDelta = amount.negate();
                feeAmount = BigDecimal.ZERO.setScale(2);
                taxAmount = BigDecimal.ZERO.setScale(2);

                portfolio.setCashBalance(toScale2(portfolio.getCashBalance().add(cashDelta)));
                tradeTransactionRepository.save(
                        TradeTransaction.builder()
                                .portfolio(portfolio)
                                .tradeType(tradeType)
                                .symbol(null)
                                .quantity(null)
                                .price(null)
                                .totalAmount(toScale2(amount))
                                .feeAmount(feeAmount)
                                .taxAmount(taxAmount)
                                .transactionDate(transactionDate)
                                .build()
                );
            }
            case BUY -> {
                symbol = normalizeSymbol(request.getSymbol());
                quantity = request.getQuantity();
                price = request.getPrice();

                if (symbol == null) throw new AppException(TradeTransactionErrorCode.TRADE_SYMBOL_REQUIRED);
                if (quantity == null) throw new AppException(TradeTransactionErrorCode.TRADE_QUANTITY_REQUIRED);
                if (quantity.compareTo(BigDecimal.ZERO) <= 0) throw new AppException(TradeTransactionErrorCode.INVALID_TRADE_QUANTITY_NON_POSITIVE);
                if (!isWholeNumber(quantity)) throw new AppException(TradeTransactionErrorCode.INVALID_TRADE_QUANTITY_MUST_BE_WHOLE_NUMBER);
                if (price == null) throw new AppException(TradeTransactionErrorCode.TRADE_PRICE_REQUIRED);
                if (price.compareTo(BigDecimal.ZERO) < 0) throw new AppException(TradeTransactionErrorCode.INVALID_TRADE_PRICE_NEGATIVE);

                BigDecimal totalAmount = quantity.multiply(price).setScale(2, RoundingMode.HALF_UP);
                if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) throw new AppException(TradeTransactionErrorCode.INVALID_TRADE_AMOUNT_NON_POSITIVE);

                feeAmount = totalAmount.multiply(feePercent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                taxAmount = BigDecimal.ZERO.setScale(2);

                cashDelta = totalAmount.add(feeAmount).negate();
                if (portfolio.getCashBalance().compareTo(totalAmount.add(feeAmount)) < 0) {
                    throw new AppException(TradeTransactionErrorCode.PORTFOLIO_CASH_BALANCE_INSUFFICIENT);
                }

                portfolio.setCashBalance(toScale2(portfolio.getCashBalance().add(cashDelta)));

                PortfolioAsset asset = portfolioAssetRepository
                        .findByPortfolio_IdAndPortfolio_UserIdAndSymbol(portfolioId, userId, symbol)
                        .orElseGet(() -> PortfolioAsset.builder()
                                .portfolio(portfolio)
                                .symbol(symbol)
                                .totalQuantity(BigDecimal.ZERO.setScale(0))
                                .averagePrice(BigDecimal.ZERO.setScale(2))
                                .build());

                BigDecimal oldQty = asset.getTotalQuantity();
                BigDecimal oldAvg = asset.getAveragePrice();

                BigDecimal newTotalQty = oldQty.add(quantity).setScale(0, RoundingMode.HALF_UP);
                BigDecimal weightedSum = oldQty.multiply(oldAvg).add(quantity.multiply(price));
                BigDecimal newAvg = weightedSum.divide(newTotalQty, 2, RoundingMode.HALF_UP);

                asset.setTotalQuantity(newTotalQty);
                asset.setAveragePrice(newAvg);
                portfolioAssetRepository.save(asset);

                tradeTransactionRepository.save(
                        TradeTransaction.builder()
                                .portfolio(portfolio)
                                .tradeType(tradeType)
                                .symbol(symbol)
                                .quantity(quantity.setScale(0, RoundingMode.HALF_UP))
                                .price(price.setScale(2, RoundingMode.HALF_UP))
                                .totalAmount(totalAmount)
                                .feeAmount(feeAmount)
                                .taxAmount(taxAmount)
                                .transactionDate(transactionDate)
                                .build()
                );
            }
            case SELL -> {
                symbol = normalizeSymbol(request.getSymbol());
                quantity = request.getQuantity();
                price = request.getPrice();

                if (symbol == null) throw new AppException(TradeTransactionErrorCode.TRADE_SYMBOL_REQUIRED);
                if (quantity == null) throw new AppException(TradeTransactionErrorCode.TRADE_QUANTITY_REQUIRED);
                if (quantity.compareTo(BigDecimal.ZERO) <= 0) throw new AppException(TradeTransactionErrorCode.INVALID_TRADE_QUANTITY_NON_POSITIVE);
                if (!isWholeNumber(quantity)) throw new AppException(TradeTransactionErrorCode.INVALID_TRADE_QUANTITY_MUST_BE_WHOLE_NUMBER);
                if (price == null) throw new AppException(TradeTransactionErrorCode.TRADE_PRICE_REQUIRED);
                if (price.compareTo(BigDecimal.ZERO) < 0) throw new AppException(TradeTransactionErrorCode.INVALID_TRADE_PRICE_NEGATIVE);

                BigDecimal totalAmount = quantity.multiply(price).setScale(2, RoundingMode.HALF_UP);
                if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) throw new AppException(TradeTransactionErrorCode.INVALID_TRADE_AMOUNT_NON_POSITIVE);

                feeAmount = totalAmount.multiply(feePercent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                taxAmount = totalAmount.multiply(taxPercent).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

                PortfolioAsset asset = portfolioAssetRepository
                        .findByPortfolio_IdAndPortfolio_UserIdAndSymbol(portfolioId, userId, symbol)
                        .orElseThrow(() -> new AppException(TradeTransactionErrorCode.PORTFOLIO_ASSET_QUANTITY_INSUFFICIENT));

                if (asset.getTotalQuantity().compareTo(quantity) < 0) {
                    throw new AppException(TradeTransactionErrorCode.PORTFOLIO_ASSET_QUANTITY_INSUFFICIENT);
                }

                cashDelta = totalAmount.subtract(feeAmount).subtract(taxAmount);
                portfolio.setCashBalance(toScale2(portfolio.getCashBalance().add(cashDelta)));

                BigDecimal newQty = asset.getTotalQuantity().subtract(quantity).setScale(0, RoundingMode.HALF_UP);
                if (newQty.compareTo(BigDecimal.ZERO) <= 0) {
                    // Keep snapshot rule: quantity==0 -> delete asset row
                    portfolioAssetRepository.delete(asset);
                } else {
                    asset.setTotalQuantity(newQty);
                    // averagePrice does NOT change on SELL
                    portfolioAssetRepository.save(asset);
                }

                tradeTransactionRepository.save(
                        TradeTransaction.builder()
                                .portfolio(portfolio)
                                .tradeType(tradeType)
                                .symbol(symbol)
                                .quantity(quantity.setScale(0, RoundingMode.HALF_UP))
                                .price(price.setScale(2, RoundingMode.HALF_UP))
                                .totalAmount(totalAmount)
                                .feeAmount(feeAmount)
                                .taxAmount(taxAmount)
                                .transactionDate(transactionDate)
                                .build()
                );
            }
            case DIVIDEND -> {
                // For now: treat DIVIDEND as snapshot-only; no cash update yet (unless UI adds it later).
                // If you want cash update for dividend, tell me and we’ll extend.
                symbol = normalizeSymbol(request.getSymbol());
                quantity = request.getQuantity();
                price = request.getPrice();

                if (symbol == null) throw new AppException(TradeTransactionErrorCode.TRADE_SYMBOL_REQUIRED);

                BigDecimal totalAmount;
                if (request.getAmount() != null) {
                    // optionally support dividend as 'amount'
                    totalAmount = request.getAmount().setScale(2, RoundingMode.HALF_UP);
                } else {
                    // fallback: 0 amount
                    totalAmount = BigDecimal.ZERO.setScale(2);
                }

                tradeTransactionRepository.save(
                        TradeTransaction.builder()
                                .portfolio(portfolio)
                                .tradeType(tradeType)
                                .symbol(symbol)
                                .quantity(quantity)
                                .price(price)
                                .totalAmount(totalAmount)
                                .feeAmount(BigDecimal.ZERO.setScale(2))
                                .taxAmount(BigDecimal.ZERO.setScale(2))
                                .transactionDate(transactionDate)
                                .build()
                );
            }
            default -> throw new AppException(TradeTransactionErrorCode.TRADE_TYPE_REQUIRED);
        }
    }

    private static String normalizeSymbol(String symbol) {
        if (symbol == null) return null;
        String s = symbol.trim().toUpperCase();
        return s.isBlank() ? null : s;
    }

    private static BigDecimal defaultIfNull(BigDecimal v, BigDecimal defaultValue) {
        return v == null ? defaultValue : v;
    }

    private static BigDecimal toScale2(BigDecimal v) {
        if (v == null) return BigDecimal.ZERO.setScale(2);
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    private static boolean isWholeNumber(BigDecimal v) {
        return v != null && v.stripTrailingZeros().scale() <= 0;
    }

    private static LocalDateTime parseDateOrNow(String transactionDate) {
        if (transactionDate == null || transactionDate.isBlank()) return LocalDateTime.now();
        try {
            // Accept ISO8601 with timezone offset.
            return OffsetDateTime.parse(transactionDate).toLocalDateTime();
        } catch (DateTimeParseException ex) {
            // Fallback: try parse as LocalDateTime without offset.
            try {
                return LocalDateTime.parse(transactionDate);
            } catch (DateTimeParseException ex2) {
                return LocalDateTime.now();
            }
        }
    }
}

