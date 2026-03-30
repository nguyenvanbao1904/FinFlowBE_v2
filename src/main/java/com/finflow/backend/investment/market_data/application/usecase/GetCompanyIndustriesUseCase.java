package com.finflow.backend.investment.market_data.application.usecase;

import com.finflow.backend.investment.market_data.domain.repository.CompanyRepository;
import com.finflow.backend.investment.market_data.presentation.response.CompanyIndustryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetCompanyIndustriesUseCase {
    private static final String UNKNOWN_INDUSTRY = "Khác";

    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public List<CompanyIndustryResponse> execute(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return List.of();
        }

        List<String> normalizedSymbols = symbols.stream()
                .filter(symbol -> symbol != null && !symbol.isBlank())
                .map(symbol -> symbol.trim().toUpperCase(Locale.ROOT))
                .distinct()
                .toList();
        if (normalizedSymbols.isEmpty()) {
            return List.of();
        }

        Map<String, String> industryBySymbol = companyRepository.findByIdInUppercase(normalizedSymbols)
                .stream()
                .collect(Collectors.toMap(
                        company -> company.getId().toUpperCase(Locale.ROOT),
                        company -> {
                            if (company.getIndustryNode() == null || company.getIndustryNode().getNameVi() == null) {
                                return UNKNOWN_INDUSTRY;
                            }
                            return company.getIndustryNode().getNameVi();
                        },
                        (first, second) -> first
                ));

        return normalizedSymbols.stream()
                .map(symbol -> CompanyIndustryResponse.builder()
                        .symbol(symbol)
                        .industryLabel(industryBySymbol.getOrDefault(symbol, UNKNOWN_INDUSTRY))
                        .build())
                .toList();
    }
}
