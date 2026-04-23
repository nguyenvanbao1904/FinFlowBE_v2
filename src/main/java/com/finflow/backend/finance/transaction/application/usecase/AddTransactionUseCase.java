package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.finance.transaction.application.port.in.AddTransactionPort;

import com.finflow.backend.finance.transaction.domain.entity.Transaction;
import com.finflow.backend.finance.transaction.application.command.AddTransactionCommand;
import com.finflow.backend.common.application.dto.UuidOutput;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AddTransactionUseCase implements AddTransactionPort {

    private final TransactionCreationHelper helper;

    @Transactional
    @Override
    public UuidOutput execute(AddTransactionCommand command) {
        Transaction saved = helper.createAndSave(command, "");
        return new UuidOutput(saved.getId());
    }
}
