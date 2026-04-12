package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.finance.transaction.application.port.in.GetInternalTransactionUserContextPort;
import com.finflow.backend.finance.transaction.domain.entity.Category;
import com.finflow.backend.finance.transaction.domain.repository.CategoryRepository;
import com.finflow.backend.finance.wealth.domain.entity.WealthAccount;
import com.finflow.backend.finance.wealth.domain.repository.WealthAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builds category/account context for the internal AI transaction API.
 * Keeps {@link com.finflow.backend.finance.transaction.presentation.controller.InternalTransactionController}
 * free of domain entity usage.
 */
@Component
@RequiredArgsConstructor
public class GetInternalTransactionUserContextUseCase implements GetInternalTransactionUserContextPort {

    private final CategoryRepository categoryRepository;
    private final WealthAccountRepository wealthAccountRepository;

    @Override
    public Map<String, Object> execute(String userId) {
        List<Category> categories = categoryRepository.findByUserIdOrSystem(userId);
        List<Map<String, Object>> categoryList = categories.stream()
                .map(c -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", c.getId().toString());
                    m.put("name", c.getName());
                    m.put("type", c.getType().name());
                    m.put("icon", c.getIcon());
                    return m;
                })
                .collect(Collectors.toList());

        List<WealthAccount> accounts = wealthAccountRepository.findAllByUserIdWithType(userId);
        List<Map<String, Object>> accountList = accounts.stream()
                .filter(a -> Boolean.TRUE.equals(a.getWealthAccountType().getIsTransactionEligible()))
                .map(a -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", a.getId().toString());
                    m.put("name", a.getName());
                    m.put("type", a.getWealthAccountType().getDisplayName());
                    m.put("balance", a.getBalance());
                    return m;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("categories", categoryList);
        result.put("accounts", accountList);
        return result;
    }
}
