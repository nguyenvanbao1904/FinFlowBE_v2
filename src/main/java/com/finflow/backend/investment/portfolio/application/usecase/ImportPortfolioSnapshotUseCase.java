package com.finflow.backend.investment.portfolio.application.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.investment.portfolio.domain.entity.Portfolio;
import com.finflow.backend.investment.portfolio.domain.entity.PortfolioAsset;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioAssetRepository;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioRepository;
import com.finflow.backend.investment.portfolio.exception.ImportPortfolioSnapshotErrorCode;
import com.finflow.backend.investment.portfolio.exception.PortfolioErrorCode;
import com.finflow.backend.investment.portfolio.presentation.request.ImportPortfolioSnapshotRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportPortfolioSnapshotUseCase {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioAssetRepository portfolioAssetRepository;

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public void execute(String userId, UUID portfolioId, ImportPortfolioSnapshotRequest request) {
        BigDecimal cashBalance = request.getCashBalance();
        if (cashBalance == null) {
            throw new AppException(ImportPortfolioSnapshotErrorCode.CASH_BALANCE_REQUIRED);
        }
        if (cashBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(ImportPortfolioSnapshotErrorCode.CASH_BALANCE_MUST_BE_NON_NEGATIVE);
        }

        Portfolio portfolio = portfolioRepository
                .findByIdAndUserId(portfolioId, userId)
                .orElseThrow(() -> new AppException(PortfolioErrorCode.PORTFOLIO_NOT_FOUND));

        List<ImportPortfolioSnapshotRequest.HoldingSnapshotRequest> holdings = request.getHoldings();
        if (holdings == null) {
            holdings = List.of();
        }

        // Normalize holdings: symbol -> holding
        Map<String, ImportPortfolioSnapshotRequest.HoldingSnapshotRequest> holdingBySymbol = holdings.stream()
                .map(h -> {
                    if (h == null) return null;
                    String symbol = normalizeSymbol(h.getSymbol());
                    return symbol == null ? null : new NormalizedHolding(symbol, h);
                })
                .filter(x -> x != null)
                .collect(Collectors.toMap(
                        x -> x.symbol,
                        x -> x.original,
                        (a, b) -> a // if duplicates, keep first
                ));

        Set<String> incomingSymbols = holdingBySymbol.keySet();

        // 1) Delete assets not in incoming list
        List<PortfolioAsset> existingAssets = portfolioAssetRepository
                .findByPortfolio_IdAndPortfolio_UserId(portfolioId, userId);
        for (PortfolioAsset asset : existingAssets) {
            if (!incomingSymbols.contains(asset.getSymbol())) {
                portfolioAssetRepository.delete(asset);
            }
        }

        // 2) Upsert incoming assets
        for (var entry : holdingBySymbol.entrySet()) {
            String symbol = entry.getKey();
            ImportPortfolioSnapshotRequest.HoldingSnapshotRequest h = entry.getValue();

            BigDecimal totalQuantity = h.getTotalQuantity();
            BigDecimal averagePrice = h.getAveragePrice();

            if (symbol == null || symbol.isBlank()) {
                throw new AppException(ImportPortfolioSnapshotErrorCode.HOLDING_SYMBOL_BLANK);
            }
            if (totalQuantity == null) {
                throw new AppException(ImportPortfolioSnapshotErrorCode.HOLDING_QUANTITY_REQUIRED);
            }
            if (totalQuantity.compareTo(BigDecimal.ZERO) < 0) {
                throw new AppException(ImportPortfolioSnapshotErrorCode.HOLDING_QUANTITY_MUST_BE_NON_NEGATIVE);
            }
            if (!isWholeNumber(totalQuantity)) {
                throw new AppException(ImportPortfolioSnapshotErrorCode.HOLDING_QUANTITY_MUST_BE_WHOLE_NUMBER);
            }
            if (averagePrice == null) {
                throw new AppException(ImportPortfolioSnapshotErrorCode.HOLDING_AVERAGE_PRICE_REQUIRED);
            }
            if (averagePrice.compareTo(BigDecimal.ZERO) < 0) {
                throw new AppException(ImportPortfolioSnapshotErrorCode.HOLDING_AVERAGE_PRICE_MUST_BE_NON_NEGATIVE);
            }

            // qty==0 means "not holding" => delete if exists
            if (totalQuantity.compareTo(BigDecimal.ZERO) == 0) {
                portfolioAssetRepository
                        .findByPortfolio_IdAndPortfolio_UserIdAndSymbol(portfolioId, userId, symbol)
                        .ifPresent(portfolioAssetRepository::delete);
                continue;
            }

            PortfolioAsset asset = portfolioAssetRepository
                    .findByPortfolio_IdAndPortfolio_UserIdAndSymbol(portfolioId, userId, symbol)
                    .orElseGet(() -> PortfolioAsset.builder()
                            .portfolio(portfolio)
                            .symbol(symbol)
                            .build());

            asset.setTotalQuantity(totalQuantity.setScale(0, RoundingMode.HALF_UP));
            asset.setAveragePrice(averagePrice.setScale(2, RoundingMode.HALF_UP));
            portfolioAssetRepository.save(asset);
        }

        portfolio.setCashBalance(cashBalance.setScale(2, RoundingMode.HALF_UP));
        portfolioRepository.save(portfolio);
    }

    private static String normalizeSymbol(String symbol) {
        if (symbol == null) return null;
        String s = symbol.trim().toUpperCase(Locale.ROOT);
        return s.isBlank() ? null : s;
    }

    private static boolean isWholeNumber(BigDecimal v) {
        return v != null && v.stripTrailingZeros().scale() <= 0;
    }

    private static class NormalizedHolding {
        final String symbol;
        final ImportPortfolioSnapshotRequest.HoldingSnapshotRequest original;

        NormalizedHolding(String symbol, ImportPortfolioSnapshotRequest.HoldingSnapshotRequest original) {
            this.symbol = symbol;
            this.original = original;
        }
    }
}

