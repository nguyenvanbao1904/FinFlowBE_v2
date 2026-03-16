package com.finflow.backend.finance.wealth.application.usecase;

import com.finflow.backend.common.exception.AppException;
import com.finflow.backend.finance.wealth.application.mapper.WealthAccountMapper;
import com.finflow.backend.finance.wealth.domain.entity.WealthAccountType;
import com.finflow.backend.finance.wealth.domain.entity.WealthAccount;
import com.finflow.backend.finance.wealth.domain.repository.WealthAccountTypeRepository;
import com.finflow.backend.finance.wealth.domain.repository.WealthAccountRepository;
import com.finflow.backend.finance.wealth.exception.WealthErrorCode;
import com.finflow.backend.finance.wealth.presentation.request.UpdateWealthAccountRequest;
import com.finflow.backend.finance.wealth.presentation.response.WealthAccountResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateWealthAccountUseCase {

    private final WealthAccountRepository wealthAccountRepository;
    private final WealthAccountTypeRepository wealthAccountTypeRepository;
    private final WealthAccountMapper wealthAccountMapper;

    @Transactional
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public WealthAccountResponse execute(String userId, UUID accountId, UpdateWealthAccountRequest request) {
        log.info("Updating wealth account {} for user: {}", accountId, userId);

        WealthAccount account = wealthAccountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new AppException(WealthErrorCode.WEALTH_ACCOUNT_NOT_FOUND));

        WealthAccountType wealthAccountType = wealthAccountTypeRepository.findById(request.getAccountTypeId())
                .orElseThrow(() -> new AppException(WealthErrorCode.WEALTH_ACCOUNT_TYPE_NOT_FOUND));

        BigDecimal balance = request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO;
        if (Boolean.FALSE.equals(wealthAccountType.getIsDebt()) && balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(WealthErrorCode.BALANCE_NEGATIVE_FOR_NON_DEBT_TYPE);
        }

        account.setName(request.getName());
        account.setWealthAccountType(wealthAccountType);
        account.setBalance(balance);
        if (request.getIncludeInNetWorth() != null) {
            account.setIncludeInNetWorth(request.getIncludeInNetWorth());
        }

        WealthAccount saved = wealthAccountRepository.save(account);
        return wealthAccountMapper.toWealthAccountResponse(saved);
    }
}
