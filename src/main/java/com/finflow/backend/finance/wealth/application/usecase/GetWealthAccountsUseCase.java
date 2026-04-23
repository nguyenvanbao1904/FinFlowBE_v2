package com.finflow.backend.finance.wealth.application.usecase;

import com.finflow.backend.finance.wealth.application.port.in.GetWealthAccountsPort;
import com.finflow.backend.finance.wealth.application.query.GetWealthAccountsQuery;

import com.finflow.backend.finance.wealth.application.dto.WealthAccountOutput;
import com.finflow.backend.finance.wealth.application.mapper.WealthAccountMapper;
import com.finflow.backend.finance.wealth.domain.repository.WealthAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetWealthAccountsUseCase implements GetWealthAccountsPort {

    private final WealthAccountRepository wealthAccountRepository;
    private final WealthAccountMapper wealthAccountMapper;

    @Transactional(readOnly = true)
    @Override
    public List<WealthAccountOutput> execute(GetWealthAccountsQuery request) {
        String userId = request.userId();
        log.info("Fetching wealth accounts for user: {}", userId);
        return wealthAccountRepository.findAllByUserIdWithType(userId).stream()
                .map(wealthAccountMapper::toWealthAccountOutput)
                .toList();
    }
}
