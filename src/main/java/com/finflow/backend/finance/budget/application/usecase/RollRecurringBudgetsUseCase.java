package com.finflow.backend.finance.budget.application.usecase;

import com.finflow.backend.finance.budget.domain.entity.Budget;
import com.finflow.backend.finance.budget.domain.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Rolls forward recurring budgets: when a recurring budget's period has ended (endDate in the past),
 * creates the next period's budget (same category, targetAmount, isRecurring, recurringStartDate).
 * Only queries budgets with endDate in a narrow window to avoid full table scan.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RollRecurringBudgetsUseCase {

    private static final int ROLL_BATCH_SIZE = 500;

    private final BudgetRepository budgetRepository;

    @Transactional
    public void execute() {
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(2);

        List<Budget> toProcess = budgetRepository
                .findByIsRecurringTrueAndEndDateBetween(from, today, PageRequest.of(0, ROLL_BATCH_SIZE))
                .getContent();

        if (toProcess.isEmpty()) {
            log.debug("No recurring budgets to roll forward");
            return;
        }

        log.info("Rolling forward {} recurring budget(s)", toProcess.size());
        int created = 0;

        for (Budget b : toProcess) {
            LocalDate nextStart = b.getEndDate().plusDays(1);
            long periodDays = ChronoUnit.DAYS.between(b.getStartDate(), b.getEndDate()) + 1;
            LocalDate nextEnd = nextStart.plusDays(periodDays - 1);

            if (budgetRepository.existsByUserIdAndCategory_IdAndStartDate(
                    b.getUserId(), b.getCategory().getId(), nextStart)) {
                continue;
            }

            LocalDate recurringStart = b.getRecurringStartDate() != null
                    ? b.getRecurringStartDate()
                    : b.getStartDate();

            Budget next = Budget.builder()
                    .userId(b.getUserId())
                    .category(b.getCategory())
                    .targetAmount(b.getTargetAmount())
                    .startDate(nextStart)
                    .endDate(nextEnd)
                    .isRecurring(true)
                    .recurringStartDate(recurringStart)
                    .build();

            budgetRepository.save(next);
            created++;
            log.debug("Created next period budget for user {} category {} from {} to {}",
                    b.getUserId(), b.getCategory().getId(), nextStart, nextEnd);
        }

        if (created > 0) {
            log.info("Rolled forward {} new budget(s)", created);
        }
    }
}
