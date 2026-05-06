package com.finflow.backend.investment.portfolio.application.usecase;

import com.finflow.backend.investment.portfolio.application.port.in.GetMonthlyNetBuyPort;
import com.finflow.backend.investment.portfolio.domain.repository.TradeTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetMonthlyNetBuyUseCase implements GetMonthlyNetBuyPort {

    private final TradeTransactionRepository tradeTransactionRepository;

    @Transactional(readOnly = true)
    @Override
    public BigDecimal execute(String userId, YearMonth month) {
        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = month.plusMonths(1).atDay(1).atStartOfDay();
        BigDecimal result = tradeTransactionRepository.sumBuyAmountByUserIdBetween(userId, start, end);
        log.debug("Monthly net buy | userId={} month={} amount={}", userId, month, result);
        return result != null ? result : BigDecimal.ZERO;
    }
}
