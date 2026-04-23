package com.finflow.backend.investment.portfolio.infrastructure;

import com.finflow.backend.investment.portfolio.api.MarketPriceQuote;
import com.finflow.backend.investment.portfolio.api.MarketPriceApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Driven adapter: delegates market price fetching to {@link VpsMarketPriceClient}.
 * Maps the infrastructure-local {@code VpsMarketPriceClient.MarketPriceQuote} to the
 * application-layer {@link MarketPriceQuote}.
 */
@Component
@RequiredArgsConstructor
public class VpsMarketPriceAdapter implements MarketPriceApi {

    private final VpsMarketPriceClient vpsMarketPriceClient;

    @Override
    public Map<String, MarketPriceQuote> getClosePrices(List<String> symbols) {
        return vpsMarketPriceClient.getClosePrices(symbols).entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> toAppModel(e.getValue())
                ));
    }

    private MarketPriceQuote toAppModel(VpsMarketPriceClient.MarketPriceQuote infra) {
        return new MarketPriceQuote(infra.priceVnd(), MarketPriceQuote.PriceSource.CLOSE);
    }
}
