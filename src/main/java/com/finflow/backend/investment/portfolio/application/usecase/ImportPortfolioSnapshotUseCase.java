package com.finflow.backend.investment.portfolio.application.usecase;

import com.finflow.backend.investment.portfolio.application.port.in.ImportPortfolioSnapshotPort;
import com.finflow.backend.investment.common.util.StockSymbolUtils;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.investment.portfolio.domain.entity.Portfolio;
import com.finflow.backend.investment.portfolio.domain.entity.PortfolioAsset;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioAssetRepository;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioRepository;
import com.finflow.backend.investment.portfolio.exception.ImportPortfolioSnapshotErrorCode;
import com.finflow.backend.investment.portfolio.exception.PortfolioErrorCode;
import com.finflow.backend.investment.portfolio.application.command.ImportPortfolioSnapshotCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImportPortfolioSnapshotUseCase implements ImportPortfolioSnapshotPort {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioAssetRepository portfolioAssetRepository;

    @Transactional
    @Override
    public void execute(ImportPortfolioSnapshotCommand command) {
        String userId = command.userId();
        UUID portfolioId = command.portfolioId();
        BigDecimal cashBalance = command.cashBalance();
        if (cashBalance == null) {
            throw new AppException(ImportPortfolioSnapshotErrorCode.CASH_BALANCE_REQUIRED);
        }
        if (cashBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(ImportPortfolioSnapshotErrorCode.CASH_BALANCE_MUST_BE_NON_NEGATIVE);
        }

        Portfolio portfolio = portfolioRepository
                .findByIdAndUserId(portfolioId, userId)
                .orElseThrow(() -> new AppException(PortfolioErrorCode.PORTFOLIO_NOT_FOUND));

        List<ImportPortfolioSnapshotCommand.HoldingSnapshot> holdings = command.holdings();
        if (holdings == null) {
            holdings = List.of();
        }

        // Normalize holdings: symbol -> holding
        Map<String, ImportPortfolioSnapshotCommand.HoldingSnapshot> holdingBySymbol = holdings.stream()
                .map(h -> {
                    if (h == null) return null;
                    String symbol = StockSymbolUtils.normalizeSymbol(h.symbol());
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
        Map<String, PortfolioAsset> existingBySymbol = existingAssets.stream()
                .collect(Collectors.toMap(PortfolioAsset::getSymbol, Function.identity()));
        List<PortfolioAsset> assetsToDelete = existingAssets.stream()
                .filter(asset -> !incomingSymbols.contains(asset.getSymbol()))
                .collect(Collectors.toList());
        portfolioAssetRepository.deleteAll(assetsToDelete);

        // 2) Upsert incoming assets
        List<PortfolioAsset> zeroQtyDeletes = new ArrayList<>();
        List<PortfolioAsset> assetsToSave = new ArrayList<>();
        for (var entry : holdingBySymbol.entrySet()) {
            String symbol = entry.getKey();
            ImportPortfolioSnapshotCommand.HoldingSnapshot h = entry.getValue();

            BigDecimal totalQuantity = h.totalQuantity();
            BigDecimal averagePrice = h.averagePrice();

            if (symbol == null || symbol.isBlank()) {
                throw new AppException(ImportPortfolioSnapshotErrorCode.HOLDING_SYMBOL_BLANK);
            }
            if (totalQuantity == null) {
                throw new AppException(ImportPortfolioSnapshotErrorCode.HOLDING_QUANTITY_REQUIRED);
            }
            if (totalQuantity.compareTo(BigDecimal.ZERO) < 0) {
                throw new AppException(ImportPortfolioSnapshotErrorCode.HOLDING_QUANTITY_MUST_BE_NON_NEGATIVE);
            }
            if (!StockSymbolUtils.isWholeNumber(totalQuantity)) {
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
                PortfolioAsset toDelete = existingBySymbol.get(symbol);
                if (toDelete != null) {
                    zeroQtyDeletes.add(toDelete);
                }
                continue;
            }

            PortfolioAsset asset = existingBySymbol.getOrDefault(symbol,
                    PortfolioAsset.builder()
                            .portfolio(portfolio)
                            .symbol(symbol)
                            .build());

            asset.setTotalQuantity(totalQuantity.setScale(0, RoundingMode.HALF_UP));
            asset.setAveragePrice(averagePrice.setScale(2, RoundingMode.HALF_UP));
            assetsToSave.add(asset);
        }
        portfolioAssetRepository.deleteAll(zeroQtyDeletes);
        portfolioAssetRepository.saveAll(assetsToSave);

        portfolio.setCashBalance(cashBalance.setScale(2, RoundingMode.HALF_UP));
        portfolioRepository.save(portfolio);
    }

    private static class NormalizedHolding {
        final String symbol;
        final ImportPortfolioSnapshotCommand.HoldingSnapshot original;

        NormalizedHolding(String symbol, ImportPortfolioSnapshotCommand.HoldingSnapshot original) {
            this.symbol = symbol;
            this.original = original;
        }
    }
}

