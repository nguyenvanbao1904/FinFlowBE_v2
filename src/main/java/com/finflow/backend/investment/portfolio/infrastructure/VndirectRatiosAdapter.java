package com.finflow.backend.investment.portfolio.infrastructure;

import com.finflow.backend.investment.portfolio.api.StockRatiosApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Driven adapter: delegates ratio fetching to {@link VndirectRatiosClient}.
 */
@Component
@RequiredArgsConstructor
public class VndirectRatiosAdapter implements StockRatiosApi {

    private final VndirectRatiosClient vndirectRatiosClient;

    @Override
    public Map<String, Double> getLatestRatios(String code, List<String> itemCodes) {
        return vndirectRatiosClient.getLatestRatios(code, itemCodes);
    }
}
