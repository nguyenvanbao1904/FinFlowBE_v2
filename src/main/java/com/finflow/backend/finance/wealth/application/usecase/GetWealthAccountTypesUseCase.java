package com.finflow.backend.finance.wealth.application.usecase;

import com.finflow.backend.finance.wealth.application.port.in.GetWealthAccountTypesPort;
import com.finflow.backend.finance.wealth.application.query.GetWealthAccountTypesQuery;

import com.finflow.backend.finance.wealth.application.dto.WealthAccountTypeOptionOutput;
import com.finflow.backend.finance.wealth.application.mapper.WealthAccountMapper;
import com.finflow.backend.finance.wealth.domain.repository.WealthAccountTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    @Override
    public List<WealthAccountTypeOptionOutput> execute(GetWealthAccountTypesQuery request) {
        log.debug("Fetching wealth account type options");
        return wealthAccountTypeRepository.findAllByOrderByCodeAsc().stream()
                .map(wealthAccountMapper::toWealthAccountTypeOptionOutput)
                .toList();
    }
}
