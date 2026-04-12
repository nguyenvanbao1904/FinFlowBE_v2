package com.finflow.backend.investment.portfolio.application.port.in;

import java.math.BigDecimal;
import com.finflow.backend.investment.portfolio.application.command.ImportPortfolioSnapshotCommand;
import java.util.List;
import java.util.Map;
import com.finflow.backend.investment.portfolio.domain.entity.PortfolioAsset;
import java.math.RoundingMode;
import java.util.UUID;
import com.finflow.backend.investment.portfolio.domain.entity.Portfolio;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.function.Function;
import java.util.Set;

public interface ImportPortfolioSnapshotPort {
    void execute(ImportPortfolioSnapshotCommand command);
}
