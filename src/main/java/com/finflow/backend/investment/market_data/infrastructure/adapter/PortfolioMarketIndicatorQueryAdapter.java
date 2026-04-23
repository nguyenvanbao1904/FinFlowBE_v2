package com.finflow.backend.investment.market_data.infrastructure.adapter;

import com.finflow.backend.investment.market_data.domain.entity.FinancialIndicator;
import com.finflow.backend.investment.market_data.domain.repository.FinancialIndicatorRepository;
import com.finflow.backend.investment.market_data.api.MarketIndicatorReadApi;
import com.finflow.backend.investment.market_data.api.MarketIndicatorData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * Adapter owned by market_data module to expose indicator queries
 * for portfolio use cases through market_data's public API contract.
 */
@Component
@RequiredArgsConstructor
public class PortfolioMarketIndicatorQueryAdapter implements MarketIndicatorReadApi {

    private final FinancialIndicatorRepository financialIndicatorRepository;

    @Override
    public List<MarketIndicatorData> findAllByCompanyIds(Collection<String> companyIds) {
        return financialIndicatorRepository
                .findByCompanyIdInOrderByCompanyIdAscYearDescQuarterDesc(companyIds)
                .stream()
                .map(this::toData)
                .toList();
    }

    private MarketIndicatorData toData(FinancialIndicator fi) {
        return new MarketIndicatorData(
                fi.getCompanyId(),
                fi.getYear(),
                fi.getQuarter(),
                fi.getPe(),
                fi.getPb(),
                fi.getPs(),
                fi.getRoe(),
                fi.getRoa()
        );
    }
}
