package com.finflow.backend.investment.portfolio.application.port.in;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.finflow.backend.investment.portfolio.application.command.CreateTradeTransactionCommand;
import java.time.OffsetDateTime;
import com.finflow.backend.investment.portfolio.domain.entity.PortfolioAsset;
import java.math.RoundingMode;
import java.util.UUID;
import com.finflow.backend.investment.portfolio.domain.entity.Portfolio;
import com.finflow.backend.investment.portfolio.domain.entity.TradeTransaction;
import java.time.format.DateTimeParseException;
import com.finflow.backend.investment.portfolio.domain.entity.TradeType;

public interface CreateTradeTransactionPort {
    void execute(CreateTradeTransactionCommand command);
}
