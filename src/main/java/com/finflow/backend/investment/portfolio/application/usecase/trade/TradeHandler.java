package com.finflow.backend.investment.portfolio.application.usecase.trade;

/**
 * Strategy interface for processing a single trade type.
 * Each implementation handles one {@link com.finflow.backend.investment.portfolio.domain.entity.TradeType}.
 */
public interface TradeHandler {

    /**
     * Executes the trade described by {@code ctx}.
     * Implementations are responsible for:
     * <ul>
     *   <li>Validating inputs specific to the trade type</li>
     *   <li>Updating portfolio cash balance and/or asset quantities</li>
     *   <li>Persisting a {@link com.finflow.backend.investment.portfolio.domain.entity.TradeTransaction} record</li>
     * </ul>
     */
    void handle(TradeContext ctx);
}
