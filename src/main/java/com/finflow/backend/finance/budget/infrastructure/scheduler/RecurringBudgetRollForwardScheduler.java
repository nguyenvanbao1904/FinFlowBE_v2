package com.finflow.backend.finance.budget.infrastructure.scheduler;

import com.finflow.backend.finance.budget.application.port.in.RollRecurringBudgetsPort;
import com.finflow.backend.finance.budget.application.command.RollRecurringBudgetsCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecurringBudgetRollForwardScheduler {

    private final RollRecurringBudgetsPort rollRecurringBudgetsUseCase;

    /** Run daily at 00:05 to create next period for recurring budgets that just ended. */
    @Scheduled(cron = "0 5 0 * * ?")
    public void rollRecurringBudgets() {
        log.info("Triggering recurring budget roll-forward job...");
        try {
            rollRecurringBudgetsUseCase.execute(new RollRecurringBudgetsCommand());
        } catch (Exception e) {
            log.error("Recurring budget roll-forward failed", e);
        }
    }
}
