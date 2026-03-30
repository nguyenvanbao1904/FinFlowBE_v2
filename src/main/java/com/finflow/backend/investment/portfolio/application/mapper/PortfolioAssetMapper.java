package com.finflow.backend.investment.portfolio.application.mapper;

import com.finflow.backend.investment.portfolio.domain.entity.PortfolioAsset;
import com.finflow.backend.investment.portfolio.presentation.response.PortfolioAssetResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PortfolioAssetMapper {
    PortfolioAssetResponse toPortfolioAssetResponse(PortfolioAsset asset);
}

