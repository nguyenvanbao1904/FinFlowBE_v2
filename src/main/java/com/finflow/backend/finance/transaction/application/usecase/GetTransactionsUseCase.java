package com.finflow.backend.finance.transaction.application.usecase;

import com.finflow.backend.finance.transaction.application.port.in.GetTransactionsPort;
import com.finflow.backend.finance.transaction.application.query.GetTransactionsQuery;

import com.finflow.backend.finance.transaction.application.dto.TransactionOutput;
import com.finflow.backend.finance.transaction.application.dto.TransactionPageOutput;
import com.finflow.backend.finance.transaction.application.mapper.TransactionMapper;
import com.finflow.backend.finance.transaction.domain.entity.Transaction;
import com.finflow.backend.finance.transaction.domain.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetTransactionsUseCase implements GetTransactionsPort {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Transactional(readOnly = true)
    @Override
    public TransactionPageOutput execute(GetTransactionsQuery request) {
        String userId = request.userId();
        int page = request.page();
        int size = Math.min(request.size(), 100);
        LocalDate startDate = request.startDate();
        LocalDate endDate = request.endDate();
        String keyword = request.keyword();
        log.info("Fetching transactions userId={}, page={}, size={}, start={}, end={}, keyword={}",
                userId, page, size, startDate, endDate, keyword);
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactions;
        
        boolean hasDateFilter = startDate != null && endDate != null;
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        
        if (hasDateFilter && hasKeyword) {
            transactions = transactionRepository.searchByUserIdAndDateRangeAndKeyword(
                    userId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59), keyword.trim(), pageable);
        } else if (hasDateFilter) {
            transactions = transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDescCreatedAtDesc(
                    userId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59), pageable);
        } else if (hasKeyword) {
            transactions = transactionRepository.searchByUserIdAndKeyword(userId, keyword.trim(), pageable);
        } else {
            transactions = transactionRepository.findByUserIdOrderByTransactionDateDescCreatedAtDesc(userId, pageable);
        }
        Page<TransactionOutput> mapped = transactions.map(transactionMapper::toTransactionOutput);
        return new TransactionPageOutput(
                mapped.getContent(),
                mapped.getTotalElements(),
                mapped.getTotalPages(),
                mapped.getNumber(),
                mapped.getSize(),
                mapped.isFirst(),
                mapped.isLast(),
                mapped.getNumberOfElements()
        );
    }
}
