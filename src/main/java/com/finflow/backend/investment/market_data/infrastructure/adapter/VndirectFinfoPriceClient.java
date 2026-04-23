package com.finflow.backend.investment.market_data.infrastructure.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

/**
 * VNDirect Finfo v4: {@code stock_prices} (close VND) and {@code vnmarket_prices} (index close).
 */
@Slf4j
@Component
public class VndirectFinfoPriceClient {

    private static final String STOCK_URL = "https://api-finfo.vndirect.com.vn/v4/stock_prices";
    private static final int RANGE_PAGE_SIZE = 500;

    private final RestClient restClient = RestClient.builder().build();
    private final ObjectMapper mapper = new ObjectMapper();

    public record StockDailyClose(LocalDate date, BigDecimal closeVnd) {}

    public List<StockDailyClose> listStockClosesInRange(String symbol, LocalDate start, LocalDate end) {
        String code = symbol == null ? "" : symbol.trim().toUpperCase();
        if (code.isEmpty() || start == null || end == null || start.isAfter(end)) {
            return List.of();
        }
        TreeMap<LocalDate, BigDecimal> byDay = new TreeMap<>();
        int page = 0;
        while (true) {
            String q = "code:%s~date:gte:%s~date:lte:%s".formatted(code, start, end);
            String uri = UriComponentsBuilder.fromUriString(STOCK_URL)
                    .queryParam("sort", "date")
                    .queryParam("q", q)
                    .queryParam("size", String.valueOf(RANGE_PAGE_SIZE))
                    .queryParam("page", String.valueOf(page))
                    .build(true)
                    .toUriString();
            List<StockDailyClose> chunk;
            try {
                String body = restClient.get()
                        .uri(uri)
                        .header("User-Agent", "Mozilla/5.0")
                        .header("Accept", "application/json")
                        .retrieve()
                        .body(String.class);
                chunk = parseStockClosesPage(body, true);
            } catch (Exception e) {
                log.warn("Finfo range fetch failed {} page {}: {}", code, page, e.getMessage());
                break;
            }
            if (chunk.isEmpty()) {
                break;
            }
            for (StockDailyClose row : chunk) {
                if (!row.date().isBefore(start) && !row.date().isAfter(end)) {
                    byDay.putIfAbsent(row.date(), row.closeVnd());
                }
            }
            if (chunk.size() < RANGE_PAGE_SIZE) {
                break;
            }
            page++;
            if (page > 200) {
                log.warn("Finfo range pagination safety stop for {}", code);
                break;
            }
        }
        return byDay.entrySet().stream()
                .map(e -> new StockDailyClose(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(StockDailyClose::date))
                .toList();
    }

    private List<StockDailyClose> parseStockClosesPage(String raw, boolean stockScaling) {
        List<StockDailyClose> out = new ArrayList<>();
        if (raw == null || raw.isBlank()) {
            return out;
        }
        try {
            JsonNode root = mapper.readTree(raw);
            JsonNode data = root.isArray() ? root : root.get("data");
            if (data == null || !data.isArray()) {
                return out;
            }
            for (JsonNode row : data) {
                LocalDate d = parseRowDate(row);
                Optional<BigDecimal> close = normalizeStockClose(row, stockScaling);
                if (d != null && close.isPresent()) {
                    out.add(new StockDailyClose(d, close.get()));
                }
            }
        } catch (Exception e) {
            log.warn("Cannot parse Finfo range payload: {}", e.getMessage());
        }
        return out;
    }

    private LocalDate parseRowDate(JsonNode row) {
        if (row == null) {
            return null;
        }
        JsonNode n = row.get("date");
        if (n == null || n.isNull()) {
            return null;
        }
        if (n.isTextual()) {
            String t = n.asText().trim();
            if (t.length() >= 10) {
                try {
                    return LocalDate.parse(t.substring(0, 10));
                } catch (Exception ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private Optional<BigDecimal> normalizeStockClose(JsonNode row, boolean stockScaling) {
        if (row == null) {
            return Optional.empty();
        }
        Double close = number(row, "close");
        if (close == null) {
            close = number(row, "adjClose");
        }
        if (close == null || close <= 0) {
            return Optional.empty();
        }
        if (stockScaling) {
            if (close > 1000) {
                return Optional.of(BigDecimal.valueOf(close).setScale(2, RoundingMode.HALF_UP));
            }
            return Optional.of(BigDecimal.valueOf(close * 1000).setScale(2, RoundingMode.HALF_UP));
        }
        return Optional.of(BigDecimal.valueOf(close).setScale(4, RoundingMode.HALF_UP));
    }

    private Double number(JsonNode node, String field) {
        if (node == null) {
            return null;
        }
        JsonNode n = node.get(field);
        if (n == null || n.isNull()) {
            return null;
        }
        if (n.isNumber()) {
            return n.asDouble();
        }
        if (n.isTextual()) {
            try {
                return Double.parseDouble(n.asText().trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
