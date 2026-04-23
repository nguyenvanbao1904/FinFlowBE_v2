package com.finflow.backend.investment.portfolio.application.mapper;

import com.finflow.backend.investment.portfolio.application.dto.PortfolioResponseOutput;
import com.finflow.backend.investment.portfolio.domain.entity.Portfolio;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface PortfolioMapper {
    PortfolioResponseOutput toPortfolioResponseOutput(Portfolio portfolio);
}

