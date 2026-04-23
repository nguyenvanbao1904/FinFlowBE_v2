package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.application.port.in.SuggestCompaniesPort;
import com.finflow.backend.investment.market_data.application.query.SuggestCompaniesQuery;
import com.finflow.backend.investment.market_data.application.service.MarketDataReadService;
import com.finflow.backend.investment.market_data.domain.entity.Company;
import com.finflow.backend.investment.market_data.application.dto.CompanySuggestionOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SuggestCompaniesUseCase implements SuggestCompaniesPort {

    private final MarketDataReadService readService;

    @Transactional(readOnly = true)
    @Override
    public List<CompanySuggestionOutput> execute(SuggestCompaniesQuery request) {
        String query = request.query();
        Integer limit = request.limit();
        String q = query == null ? "" : query.trim();
        if (q.isBlank()) return List.of();

        int resolvedLimit = resolveLimit(limit);
        String normalized = q.toUpperCase(Locale.ROOT);

        Map<String, Company> unique = new LinkedHashMap<>();

        List<Company> prefix = readService.suggestBySymbolPrefix(normalized, resolvedLimit);
        for (Company c : prefix) {
            unique.putIfAbsent(c.getId(), c);
        }

        if (unique.size() < resolvedLimit) {
            int remaining = resolvedLimit - unique.size();
            List<Company> byName = readService.suggestByName(q, resolvedLimit * 2); // fetch more to allow de-dupe
            for (Company c : byName) {
                if (unique.size() >= resolvedLimit) break;
                unique.putIfAbsent(c.getId(), c);
            }
        }

        List<CompanySuggestionOutput> out = new ArrayList<>(unique.size());
        for (Company c : unique.values()) {
            out.add(new CompanySuggestionOutput(c.getId(), c.getCompanyName()));
        }
        return out;
    }

    private static int resolveLimit(Integer limit) {
        int l = limit == null ? 10 : limit;
        if (l <= 0) return 10;
        return Math.min(l, 20);
    }
}
