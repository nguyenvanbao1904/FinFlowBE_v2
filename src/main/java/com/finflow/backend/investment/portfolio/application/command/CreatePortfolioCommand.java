package com.finflow.backend.investment.portfolio.application.command;

/**
 * Command for creating a new portfolio.
 */
public record CreatePortfolioCommand(
        String userId,
        String name,
        java.util.UUID wealthAccountId
) {}
