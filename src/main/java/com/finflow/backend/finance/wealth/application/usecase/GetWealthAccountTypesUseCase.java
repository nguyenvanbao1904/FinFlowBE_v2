package com.finflow.backend.finance.wealth.application.usecase;

import com.finflow.backend.finance.wealth.application.port.in.GetWealthAccountTypesPort;

import com.finflow.backend.finance.wealth.application.mapper.WealthAccountMapper;
import com.finflow.backend.finance.wealth.domain.repository.WealthAccountTypeRepository;
import com.finflow.backend.finance.wealth.presentation.response.WealthAccountTypeOptionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetWealthAccountTypesUseCase implements GetWealthAccountTypesPort {

    private final WealthAccountTypeRepository wealthAccountTypeRepository;
    private final WealthAccountMapper wealthAccountMapper;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Override
    public List<WealthAccountTypeOptionResponse> execute() {
        log.debug("Fetching wealth account type options");
        return wealthAccountTypeRepository.findAllByOrderByCodeAsc().stream()
                .map(wealthAccountMapper::toWealthAccountTypeOptionResponse)
                .toList();
    }
}
