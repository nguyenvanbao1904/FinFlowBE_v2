package com.finflow.backend.transaction.application.mapper;

import com.finflow.backend.transaction.domain.entity.Transaction;
import com.finflow.backend.transaction.presentation.response.TransactionResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface TransactionMapper {
    TransactionResponse toTransactionResponse(Transaction transaction);
}
