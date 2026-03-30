package com.finflow.backend.investment.portfolio.application.mapper;

import com.finflow.backend.investment.portfolio.domain.entity.Portfolio;
import com.finflow.backend.investment.portfolio.presentation.response.PortfolioResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PortfolioMapper {
    PortfolioResponse toPortfolioResponse(Portfolio portfolio);
}

