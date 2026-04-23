package com.finflow.backend.investment.portfolio.application.port.in;

import com.finflow.backend.investment.portfolio.application.command.ImportPortfolioSnapshotCommand;

public interface ImportPortfolioSnapshotPort {
    void execute(ImportPortfolioSnapshotCommand command);
}
