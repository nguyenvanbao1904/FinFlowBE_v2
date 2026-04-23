package com.finflow.backend.investment.market_data.infrastructure.adapter;

import com.finflow.backend.investment.market_data.application.model.StockDailyClose;
import com.finflow.backend.investment.market_data.application.port.out.FetchHistoricalPricePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class VndirectFinfoPriceAdapter implements FetchHistoricalPricePort {

    private final VndirectFinfoPriceClient vndirectFinfoPriceClient;

    @Override
    public List<StockDailyClose> listStockClosesInRange(String symbol, LocalDate start, LocalDate end) {
        return vndirectFinfoPriceClient.listStockClosesInRange(symbol, start, end).stream()
                .map(c -> new StockDailyClose(c.date(), c.closeVnd()))
                .toList();
    }
}
