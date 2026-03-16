package com.finflow.backend.finance.transaction.application.mapper;

import com.finflow.backend.finance.transaction.domain.entity.Transaction;
import com.finflow.backend.finance.transaction.presentation.response.TransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface TransactionMapper {

    @Mapping(source = "wealthAccount.id", target = "accountId")
    TransactionResponse toTransactionResponse(Transaction transaction);
}
