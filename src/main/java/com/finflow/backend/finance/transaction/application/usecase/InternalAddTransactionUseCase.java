package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.finance.transaction.application.command.AddTransactionCommand;
import com.finflow.backend.finance.transaction.application.dto.TransactionOutput;
import com.finflow.backend.finance.transaction.application.mapper.TransactionMapper;
import com.finflow.backend.finance.transaction.application.port.in.InternalAddTransactionPort;
import com.finflow.backend.finance.transaction.domain.entity.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Internal version of {@link AddTransactionUseCase} — called by AI agent via internal API.
 * NO {@code @PreAuthorize} — security is handled by X-Internal-Api-Key at the filter level.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InternalAddTransactionUseCase implements InternalAddTransactionPort {

    private final TransactionCreationHelper helper;
    private final TransactionMapper transactionMapper;

    @Transactional
    @Override
    public TransactionOutput execute(AddTransactionCommand command) {
        Transaction saved = helper.createAndSave(command, "[INTERNAL] ");
        return transactionMapper.toTransactionOutput(saved);
    }
}
