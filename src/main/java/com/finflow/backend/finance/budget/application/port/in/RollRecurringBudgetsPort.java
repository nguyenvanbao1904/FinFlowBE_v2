package com.finflow.backend.finance.budget.application.port.in;

import java.util.List;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import com.finflow.backend.finance.budget.domain.entity.Budget;
import org.springframework.data.domain.PageRequest;

public interface RollRecurringBudgetsPort {
    void execute();
}
