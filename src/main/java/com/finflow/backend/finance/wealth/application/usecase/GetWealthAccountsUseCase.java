package com.finflow.backend.finance.wealth.application.usecase;

import com.finflow.backend.finance.wealth.application.mapper.WealthAccountMapper;
import com.finflow.backend.finance.wealth.domain.repository.WealthAccountRepository;
import com.finflow.backend.finance.wealth.presentation.response.WealthAccountResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetWealthAccountsUseCase {

    private final WealthAccountRepository wealthAccountRepository;
    private final WealthAccountMapper wealthAccountMapper;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<WealthAccountResponse> execute(String userId) {
        log.info("Fetching wealth accounts for user: {}", userId);
        return wealthAccountRepository.findAllByUserIdWithType(userId).stream()
                .map(wealthAccountMapper::toWealthAccountResponse)
                .toList();
    }
}
