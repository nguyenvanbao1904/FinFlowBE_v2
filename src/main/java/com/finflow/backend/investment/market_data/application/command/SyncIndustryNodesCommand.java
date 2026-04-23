package com.finflow.backend.investment.market_data.application.command;

import com.finflow.backend.investment.market_data.application.dto.IndustryNodeRequestInput;

import java.util.List;

public record SyncIndustryNodesCommand(
        List<IndustryNodeRequestInput> request
) {}
