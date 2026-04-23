package com.finflow.backend.investment.portfolio.infrastructure;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.investment.portfolio.exception.PortfolioErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lấy giá đóng cửa gần nhất từ VPS Data Feed (không realtime).
 *
 * <p>Normalize đơn vị: VPS có thể trả close theo format thập phân nhỏ (vd: 23.8)
 * hoặc VND đầy đủ (vd: "23600.0"). Backend luôn trả về VND cho iOS.
 *
 * <p>Cache in-memory với TTL {@value #CACHE_TTL_MS} ms (30 phút) để tránh
 * spam API khi nhiều user cùng mở Portfolio tab.
 */
@Slf4j
@Component
public class VpsMarketPriceClient {

    private static final String BASE_URL = "https://bgapidatafeed.vps.com.vn";
    private static final long CACHE_TTL_MS = 30 * 60 * 1_000L;

    private final RestClient restClient;
    private final ConcurrentHashMap<String, CachedPrice> cache = new ConcurrentHashMap<>();

    public VpsMarketPriceClient() {
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .build();
    }

    /**
     * Trả về map symbol → quote (giá + nguồn giá).
     * Nếu symbol không lấy được giá → bỏ qua (FE sẽ thấy thiếu key và hiện "-").
     */
    public Map<String, MarketPriceQuote> getClosePrices(List<String> symbols) {
        Map<String, MarketPriceQuote> result = new HashMap<>();
        for (String symbol : symbols) {
            try {
                result.put(symbol, getClosePrice(symbol));
            } catch (Exception e) {
                log.warn("VPS price fetch failed for {}: {}", symbol, e.getMessage());
            }
        }
        return result;
    }

    private MarketPriceQuote getClosePrice(String symbol) {
        CachedPrice cached = cache.get(symbol);
        if (cached != null && !cached.isExpired()) {
            return cached.quote;
        }

        VpsTicker[] tickers = restClient.get()
                .uri("/getliststockdata/{symbol}", symbol.toUpperCase())
                .retrieve()
                .body(VpsTicker[].class);

        if (tickers == null || tickers.length == 0) {
            throw new AppException(PortfolioErrorCode.MARKET_PRICE_EMPTY_RESPONSE);
        }

        MarketPriceQuote quote = normalize(tickers[0]);
        cache.put(symbol, new CachedPrice(quote));
        return quote;
    }

    /**
     * Chỉ dùng closePrice (giá đóng cửa gần nhất, không realtime).
     */
    private MarketPriceQuote normalize(VpsTicker ticker) {
        if (ticker.closePrice() != null && !ticker.closePrice().isBlank()) {
            try {
                double close = Double.parseDouble(ticker.closePrice().trim());
                if (close > 1000) {
                    return new MarketPriceQuote(close, PriceSource.CLOSE);
                }
                // closePrice nhỏ thì hiểu là nghìn VND
                return new MarketPriceQuote(close * 1000, PriceSource.CLOSE);
            } catch (NumberFormatException ignored) {
                throw new AppException(PortfolioErrorCode.MARKET_PRICE_PARSE_FAILED);
            }
        }
        throw new AppException(PortfolioErrorCode.MARKET_PRICE_MISSING);
    }

    // --- Inner types ---

    private record CachedPrice(MarketPriceQuote quote, long createdAt) {
        CachedPrice(MarketPriceQuote quote) {
            this(quote, System.currentTimeMillis());
        }
        boolean isExpired() {
            return System.currentTimeMillis() - createdAt > CACHE_TTL_MS;
        }
    }

    public enum PriceSource { CLOSE }

    public record MarketPriceQuote(double priceVnd, PriceSource source) {}

    /**
     * Minimal projection của VPS ticker JSON. Chỉ lấy các field cần thiết.
     * Jackson sẽ bỏ qua các field lạ còn lại nhờ {@code DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES = false}.
     */
    private record VpsTicker(String closePrice) {}
}
