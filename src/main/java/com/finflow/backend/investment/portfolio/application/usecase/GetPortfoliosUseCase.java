package com.finflow.backend.investment.portfolio.application.usecase;

import com.finflow.backend.investment.portfolio.application.mapper.PortfolioMapper;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioAssetRepository;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioRepository;
import com.finflow.backend.investment.portfolio.domain.repository.PortfolioStockCostBasisProjection;
import com.finflow.backend.investment.portfolio.presentation.response.PortfolioResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetPortfoliosUseCase {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioAssetRepository portfolioAssetRepository;
    private final PortfolioMapper portfolioMapper;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<PortfolioResponse> execute(String userId) {
        log.info("Getting portfolios for user: {}", userId);
        var portfolios = portfolioRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (portfolios.isEmpty()) {
            return List.of();
        }

        List<UUID> portfolioIds = portfolios.stream().map(p -> p.getId()).toList();
        Map<UUID, java.math.BigDecimal> stockCostBasisByPortfolioId = portfolioAssetRepository
                .sumStockCostBasisByUserAndPortfolioIds(userId, portfolioIds)
                .stream()
                .collect(Collectors.toMap(
                        PortfolioStockCostBasisProjection::getPortfolioId,
                        row -> row.getStockCostBasis() == null ? java.math.BigDecimal.ZERO : row.getStockCostBasis()
                ));

        return portfolios.stream()
                .map(portfolio -> {
                    PortfolioResponse response = portfolioMapper.toPortfolioResponse(portfolio);
                    java.math.BigDecimal stockCostBasis = stockCostBasisByPortfolioId.getOrDefault(
                            portfolio.getId(),
                            java.math.BigDecimal.ZERO
                    );
                    response.setTotalCostBasis(portfolio.getCashBalance().add(stockCostBasis));
                    return response;
                })
                .toList();
    }
}

