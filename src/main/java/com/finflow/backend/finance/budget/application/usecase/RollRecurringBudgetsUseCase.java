package com.finflow.backend.finance.budget.application.usecase;

import com.finflow.backend.finance.budget.application.command.RollRecurringBudgetsCommand;
import com.finflow.backend.finance.budget.application.port.in.RollRecurringBudgetsPort;

import com.finflow.backend.finance.budget.domain.entity.Budget;
import com.finflow.backend.finance.budget.domain.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Rolls forward recurring budgets: when a recurring budget's period has ended (endDate in the past),
 * creates the next period's budget (same category, targetAmount, isRecurring, recurringStartDate).
 * Only queries budgets with endDate in a narrow window to avoid full table scan.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RollRecurringBudgetsUseCase implements RollRecurringBudgetsPort {

    private static final int ROLL_BATCH_SIZE = 500;

    private final BudgetRepository budgetRepository;

    @Transactional
    @Override
    public void execute(RollRecurringBudgetsCommand request) {
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

        // --- compute next-period metadata for every candidate in-memory ---
        record NextPeriod(Budget source, LocalDate nextStart, LocalDate nextEnd) {}
        List<NextPeriod> candidates = toProcess.stream()
                .map(b -> {
                    LocalDate nextStart = b.getEndDate().plusDays(1);
                    long periodDays = ChronoUnit.DAYS.between(b.getStartDate(), b.getEndDate()) + 1;
                    return new NextPeriod(b, nextStart, nextStart.plusDays(periodDays - 1));
                })
                .toList();

        // --- single bulk query to find which (userId, categoryId, startDate) already exist ---
        Set<String> userIds = candidates.stream()
                .map(c -> c.source().getUserId())
                .collect(Collectors.toSet());
        Set<UUID> categoryIds = candidates.stream()
                .map(c -> c.source().getCategoryId())
                .collect(Collectors.toSet());
        Set<LocalDate> startDates = candidates.stream()
                .map(NextPeriod::nextStart)
                .collect(Collectors.toSet());

        Set<String> existingKeys = budgetRepository
                .findByUserIdInAndCategoryIdInAndStartDateIn(userIds, categoryIds, startDates)
                .stream()
                .map(b -> b.getUserId() + "|" + b.getCategoryId() + "|" + b.getStartDate())
                .collect(Collectors.toCollection(HashSet::new));

        // --- build new budgets (filter duplicates in-memory) ---
        List<Budget> newBudgets = new ArrayList<>();
        for (NextPeriod c : candidates) {
            String key = c.source().getUserId() + "|" + c.source().getCategoryId() + "|" + c.nextStart();
            if (existingKeys.contains(key)) {
                continue;
            }

            LocalDate recurringStart = c.source().getRecurringStartDate() != null
                    ? c.source().getRecurringStartDate()
                    : c.source().getStartDate();

            newBudgets.add(Budget.builder()
                    .userId(c.source().getUserId())
                    .categoryId(c.source().getCategoryId())
                    .targetAmount(c.source().getTargetAmount())
                    .startDate(c.nextStart())
                    .endDate(c.nextEnd())
                    .isRecurring(true)
                    .recurringStartDate(recurringStart)
                    .build());

            log.debug("Queuing next period budget for user {} category {} from {} to {}",
                    c.source().getUserId(), c.source().getCategoryId(), c.nextStart(), c.nextEnd());
        }

        if (!newBudgets.isEmpty()) {
            budgetRepository.saveAll(newBudgets);
            log.info("Rolled forward {} new budget(s)", newBudgets.size());
        }
    }
}
