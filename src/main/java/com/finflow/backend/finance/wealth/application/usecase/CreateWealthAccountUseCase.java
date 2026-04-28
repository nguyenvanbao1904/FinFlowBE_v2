package com.finflow.backend.finance.wealth.application.usecase;

import com.finflow.backend.finance.wealth.application.port.in.CreateWealthAccountPort;

import com.finflow.backend.common.exception.AppException;

import com.finflow.backend.finance.wealth.domain.entity.WealthAccountType;
import com.finflow.backend.finance.wealth.domain.entity.WealthAccount;
import com.finflow.backend.finance.wealth.domain.repository.WealthAccountRepository;
import com.finflow.backend.finance.wealth.domain.repository.WealthAccountTypeRepository;
import com.finflow.backend.finance.wealth.exception.WealthErrorCode;
import com.finflow.backend.finance.wealth.application.command.CreateWealthAccountCommand;
import com.finflow.backend.finance.wealth.application.dto.WealthAccountOutput;
import com.finflow.backend.finance.wealth.application.mapper.WealthAccountMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreateWealthAccountUseCase implements CreateWealthAccountPort {

    private final WealthAccountRepository wealthAccountRepository;
    private final WealthAccountTypeRepository wealthAccountTypeRepository;
    private final WealthAccountMapper wealthAccountMapper;

    @Transactional
    @Override
    public WealthAccountOutput execute(CreateWealthAccountCommand command) {
        String userId = command.userId();
        log.info("Creating wealth account for user: {}", userId);

        WealthAccountType wealthAccountType = wealthAccountTypeRepository.findById(command.accountTypeId())
                .orElseThrow(() -> new AppException(WealthErrorCode.WEALTH_ACCOUNT_TYPE_NOT_FOUND));

        BigDecimal balance = command.balance() != null ? command.balance() : BigDecimal.ZERO;
        if (Boolean.FALSE.equals(wealthAccountType.getIsDebt()) && balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(WealthErrorCode.BALANCE_NEGATIVE_FOR_NON_DEBT_TYPE);
        }

        Boolean includeInNetWorth = command.includeInNetWorth();
        WealthAccount account = WealthAccount.builder()
                .userId(userId)
                .name(command.name())
                .wealthAccountType(wealthAccountType)
                .balance(balance)
                .isSynced(false)
                .includeInNetWorth(includeInNetWorth != null ? includeInNetWorth : Boolean.TRUE)
                .build();

        WealthAccount saved = wealthAccountRepository.save(account);
        return wealthAccountMapper.toWealthAccountOutput(saved);
    }
}
