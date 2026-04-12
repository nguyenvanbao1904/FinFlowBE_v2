package com.finflow.backend.finance.wealth.application.usecase;

import com.finflow.backend.finance.wealth.application.port.in.UpdateWealthAccountPort;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.finance.wealth.application.mapper.WealthAccountMapper;
import com.finflow.backend.finance.wealth.domain.entity.WealthAccountType;
import com.finflow.backend.finance.wealth.domain.entity.WealthAccount;
import com.finflow.backend.finance.wealth.domain.repository.WealthAccountTypeRepository;
import com.finflow.backend.finance.wealth.domain.repository.WealthAccountRepository;
import com.finflow.backend.finance.wealth.exception.WealthErrorCode;
import com.finflow.backend.finance.wealth.application.command.UpdateWealthAccountCommand;
import com.finflow.backend.finance.wealth.presentation.response.WealthAccountResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UpdateWealthAccountUseCase implements UpdateWealthAccountPort {

    private final WealthAccountRepository wealthAccountRepository;
    private final WealthAccountTypeRepository wealthAccountTypeRepository;
    private final WealthAccountMapper wealthAccountMapper;

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Override
    public WealthAccountResponse execute(UpdateWealthAccountCommand command) {
        String userId = command.userId();
        UUID accountId = command.accountId();
        log.info("Updating wealth account {} for user {}", accountId, userId);

        WealthAccount account = wealthAccountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new AppException(WealthErrorCode.WEALTH_ACCOUNT_NOT_FOUND));

        WealthAccountType accountType = wealthAccountTypeRepository.findById(command.accountTypeId())
                .orElseThrow(() -> new AppException(WealthErrorCode.WEALTH_ACCOUNT_TYPE_NOT_FOUND));

        BigDecimal balance = command.balance() != null ? command.balance() : BigDecimal.ZERO;
        if (Boolean.FALSE.equals(accountType.getIsDebt()) && balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(WealthErrorCode.BALANCE_NEGATIVE_FOR_NON_DEBT_TYPE);
        }

        account.setName(command.name());
        account.setWealthAccountType(accountType);
        account.setBalance(balance);
        if (command.includeInNetWorth() != null) {
            account.setIncludeInNetWorth(command.includeInNetWorth());
        }

        WealthAccount saved = wealthAccountRepository.save(account);
        return wealthAccountMapper.toWealthAccountResponse(saved);
    }
}
