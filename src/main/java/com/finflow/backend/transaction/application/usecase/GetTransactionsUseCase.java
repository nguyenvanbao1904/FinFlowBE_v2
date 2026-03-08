package com.finflow.backend.transaction.application.usecase;

import com.finflow.backend.transaction.application.mapper.TransactionMapper;
import com.finflow.backend.transaction.domain.entity.Transaction;
import com.finflow.backend.transaction.domain.repository.TransactionRepository;
import com.finflow.backend.transaction.presentation.response.TransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class GetTransactionsUseCase {

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Page<TransactionResponse> execute(String userId, int page, int size,
                                             LocalDate startDate, LocalDate endDate, String keyword) {
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
            transactions = transactionRepository.findByUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                    userId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59), pageable);
        } else if (hasKeyword) {
            transactions = transactionRepository.searchByUserIdAndKeyword(userId, keyword.trim(), pageable);
        } else {
            transactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(userId, pageable);
        }
        return transactions.map(transactionMapper::toTransactionResponse);
    }
}
