package com.finflow.backend.finance.dashboard.application.port.in;

import com.finflow.backend.finance.dashboard.application.dto.HomeInsightOutput;
import com.finflow.backend.finance.dashboard.application.dto.HomeInsightSnapshot;

public interface GenerateHomeInsightPort {
    HomeInsightOutput execute(HomeInsightSnapshot snapshot);
}
