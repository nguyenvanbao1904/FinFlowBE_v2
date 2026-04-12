package com.finflow.backend.investment.portfolio.infrastructure;

import com.finflow.backend.investment.market_data.domain.entity.FinancialIndicator;
import com.finflow.backend.investment.market_data.domain.repository.FinancialIndicatorRepository;
import com.finflow.backend.investment.portfolio.application.port.out.MarketIndicatorQueryPort;
import com.finflow.backend.investment.portfolio.application.result.MarketIndicatorData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * Adapter that fulfills {@link MarketIndicatorQueryPort} by delegating to
 * {@link FinancialIndicatorRepository} from the market_data context.
 *
 * <p>Lives in portfolio.infrastructure so that cross-context JPA coupling
 * is confined to the infrastructure layer, not the application layer.
 */
@Component
@RequiredArgsConstructor
public class MarketIndicatorQueryAdapter implements MarketIndicatorQueryPort {

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
