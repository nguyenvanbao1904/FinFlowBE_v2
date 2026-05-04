package com.finflow.backend.investment.portfolio.application.port.in;

import com.finflow.backend.investment.portfolio.application.dto.TradeTransactionOutput;
import com.finflow.backend.investment.portfolio.application.query.GetTradeTransactionsQuery;
import org.springframework.data.domain.Page;

public interface GetTradeTransactionsPort {
    Page<TradeTransactionOutput> execute(GetTradeTransactionsQuery query);
}
