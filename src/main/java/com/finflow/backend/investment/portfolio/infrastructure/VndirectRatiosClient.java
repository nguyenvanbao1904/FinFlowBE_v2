package com.finflow.backend.investment.portfolio.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight client for VNDirect ratios API (single official endpoint).
 */
@Slf4j
@Component
public class VndirectRatiosClient {

    private static final long CACHE_TTL_MS = 30 * 60 * 1_000L;

    private static final String PRIMARY_ENDPOINT_TEMPLATE =
            "https://api-finfo.vndirect.com.vn/v4/ratios/latest?order=reportDate&where=code:%s&filter=itemCode:%s";

    private final RestClient restClient = RestClient.builder().build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final ConcurrentHashMap<String, CachedValue> cache = new ConcurrentHashMap<>();

    public Map<String, Double> getLatestRatios(String code, List<String> itemCodes) {
        Map<String, Double> result = new HashMap<>();
        if (itemCodes == null || itemCodes.isEmpty()) {
            return result;
        }

        String normalizedCode = code == null || code.isBlank() ? "VNINDEX" : code.trim().toUpperCase();
        String joinedItemCodes = String.join(",", itemCodes);
        String batchCacheKey = normalizedCode + "|" + joinedItemCodes;
        CachedValue cached = cache.get(batchCacheKey);
        if (cached != null && !cached.isExpired() && cached.mapValue() != null) {
            return cached.mapValue();
        }

        String safeCode = normalizedCode;
        String safeItemCodes = joinedItemCodes;
        String url = PRIMARY_ENDPOINT_TEMPLATE.formatted(safeCode, safeItemCodes);
        try {
            String body = restClient.get()
                    .uri(url)
                    .header("User-Agent", "Mozilla/5.0")
                    .header("Accept", "application/json")
                    .retrieve()
                    .body(String.class);
            Map<String, Double> parsed = parseBatchRatios(body, itemCodes);
            if (!parsed.isEmpty()) {
                // Avoid poisoning cache with partial payloads (e.g. only PE returned).
                // Cache only when all requested itemCodes are present.
                if (parsed.keySet().containsAll(itemCodes)) {
                    cache.put(batchCacheKey, CachedValue.ofMap(parsed));
                }
                return parsed;
            }
        } catch (Exception e) {
            log.warn("VNDirect fetch failed for {} via {}: {}", normalizedCode, url, e.getMessage());
        }

        log.warn("Cannot resolve VNDirect ratios for code={}, itemCodes={}", normalizedCode, joinedItemCodes);
        return result;
    }

    private Map<String, Double> parseBatchRatios(String raw, List<String> wantedItemCodes) {
        Map<String, Double> result = new HashMap<>();
        if (raw == null || raw.isBlank()) {
            return result;
        }
        try {
            JsonNode root = mapper.readTree(raw);
            JsonNode dataNode = root;
            if (!root.isArray() && root.has("data") && root.get("data").isArray()) {
                dataNode = root.get("data");
            }
            if (!dataNode.isArray()) {
                return result;
            }

            List<String> normalizedWanted = wantedItemCodes.stream()
                    .map(String::trim)
                    .toList();

            // keep latest reportDate per itemCode if duplicated rows returned
            Map<String, LocalDate> bestDateByCode = new HashMap<>();
            for (JsonNode row : dataNode) {
                String itemCode = text(row, "itemCode");
                if (itemCode != null) {
                    itemCode = itemCode.trim();
                }
                if (itemCode == null || !normalizedWanted.contains(itemCode)) {
                    continue;
                }
                Double value = number(row, "value");
                if (value == null) value = number(row, "ratioValue");
                if (value == null) value = number(row, "numericValue");
                if (value == null) {
                    continue;
                }

                LocalDate date = parseDate(text(row, "reportDate"));
                LocalDate prev = bestDateByCode.get(itemCode);
                if (prev == null || (date != null && date.isAfter(prev))) {
                    result.put(itemCode, value);
                    if (date != null) {
                        bestDateByCode.put(itemCode, date);
                    }
                } else if (prev == null) {
                    // no reportDate, keep first available value
                    result.putIfAbsent(itemCode, value);
                }
            }
            return result;
        } catch (Exception e) {
            log.warn("Cannot parse VNDirect ratios payload: {}", e.getMessage());
            return result;
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode n = node.get(field);
        if (n == null || n.isNull()) return null;
        return n.asText();
    }

    private Double number(JsonNode node, String field) {
        JsonNode n = node.get(field);
        if (n == null || n.isNull()) return null;
        if (n.isNumber()) return n.asDouble();
        if (n.isTextual()) {
            try {
                return Double.parseDouble(n.asText().trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private record CachedValue(Double value, Map<String, Double> mapValue, long createdAt) {
        static CachedValue ofMap(Map<String, Double> mapValue) {
            return new CachedValue(null, Map.copyOf(mapValue), System.currentTimeMillis());
        }

        boolean isExpired() {
            return System.currentTimeMillis() - createdAt > CACHE_TTL_MS;
        }
    }
}

