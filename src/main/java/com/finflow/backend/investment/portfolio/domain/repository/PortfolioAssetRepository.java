package com.finflow.backend.investment.portfolio.domain.repository;

import com.finflow.backend.investment.portfolio.domain.entity.PortfolioAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioAssetRepository extends JpaRepository<PortfolioAsset, UUID> {

    List<PortfolioAsset> findByPortfolio_IdAndPortfolio_UserId(UUID portfolioId, String userId);

    Optional<PortfolioAsset> findByPortfolio_IdAndPortfolio_UserIdAndSymbol(
            UUID portfolioId,
            String userId,
            String symbol
    );

    @Query("""
            select pa.portfolio.id as portfolioId,
                   coalesce(sum(pa.totalQuantity * pa.averagePrice), 0) as stockCostBasis
            from PortfolioAsset pa
            where pa.portfolio.userId = :userId
              and pa.portfolio.id in :portfolioIds
            group by pa.portfolio.id
            """)
    List<PortfolioStockCostBasisProjection> sumStockCostBasisByUserAndPortfolioIds(
            @Param("userId") String userId,
            @Param("portfolioIds") List<UUID> portfolioIds
    );

    void deleteByPortfolio_Id(UUID portfolioId);
}

