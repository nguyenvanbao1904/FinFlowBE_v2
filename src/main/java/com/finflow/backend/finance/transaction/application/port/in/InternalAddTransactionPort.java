package com.finflow.backend.finance.transaction.application.port.in;

import com.finflow.backend.finance.transaction.application.command.AddTransactionCommand;
import com.finflow.backend.finance.transaction.presentation.response.TransactionResponse;

/**
 * Internal (service-to-service) add transaction — same payload as {@link AddTransactionPort} but no JWT security on the use case.
 */
public interface InternalAddTransactionPort {

    TransactionResponse execute(AddTransactionCommand command);
}
