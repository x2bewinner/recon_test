package com.financial.recon.service;

import com.financial.recon.repository.TransactionTotalSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionTotalSummaryServiceImpl implements TransactionTotalSummaryService {

    private final TransactionTotalSummaryRepository transactionTotalSummaryRepository;

    @Override
    public boolean calculateTransactionTotal(LocalDate settlementDate) {
        try {
            log.info("Service: Starting transaction total calculation, settlement date: {}", settlementDate);
            int affectedRows = transactionTotalSummaryRepository.insertTransactionTotals(settlementDate);
            boolean result = affectedRows >= 0; // 0 rows is also considered success (no data)
            
            if (result) {
                log.info("Service: Transaction total calculation succeeded, settlement date: {}", settlementDate);
            } else {
                log.warn("Service: Transaction total calculation failed, settlement date: {}", settlementDate);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Service: Exception occurred during transaction total calculation, settlement date: {}", settlementDate, e);
            throw new RuntimeException("Failed to calculate transaction total: " + e.getMessage(), e);
        }
    }
}

