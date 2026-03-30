package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.domain.entity.Company;
import com.finflow.backend.investment.market_data.domain.repository.CompanyRepository;
import com.finflow.backend.investment.market_data.presentation.response.CompanySuggestionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuggestCompaniesUseCase {

    private final CompanyRepository companyRepository;

    public List<CompanySuggestionResponse> execute(String query, Integer limit) {
        String q = query == null ? "" : query.trim();
        if (q.isBlank()) return List.of();

        int resolvedLimit = resolveLimit(limit);
        String normalized = q.toUpperCase(Locale.ROOT);

        Map<String, Company> unique = new LinkedHashMap<>();

        List<Company> prefix = companyRepository.findByIdStartingWithIgnoreCaseOrderByIdAsc(
                normalized,
                PageRequest.of(0, resolvedLimit)
        );
        for (Company c : prefix) {
            unique.putIfAbsent(c.getId(), c);
        }

        if (unique.size() < resolvedLimit) {
            int remaining = resolvedLimit - unique.size();
            List<Company> byName = companyRepository.findByCompanyNameContainingIgnoreCaseOrderByIdAsc(
                    q,
                    PageRequest.of(0, resolvedLimit * 2) // fetch more to allow de-dupe
            );
            for (Company c : byName) {
                if (unique.size() >= resolvedLimit) break;
                unique.putIfAbsent(c.getId(), c);
            }
        }

        List<CompanySuggestionResponse> out = new ArrayList<>(unique.size());
        for (Company c : unique.values()) {
            out.add(new CompanySuggestionResponse(c.getId(), c.getCompanyName()));
        }
        return out;
    }

    private static int resolveLimit(Integer limit) {
        int l = limit == null ? 10 : limit;
        if (l <= 0) return 10;
        return Math.min(l, 20);
    }
}

