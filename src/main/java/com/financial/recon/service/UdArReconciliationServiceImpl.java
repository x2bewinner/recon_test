package com.financial.recon.service;

import com.financial.recon.repository.UdArReconciliationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class UdArReconciliationServiceImpl implements UdArReconciliationService {

    private final UdArReconciliationRepository udArReconciliationRepository;

    @Override
    public boolean executeReconciliation(LocalDate settlementDate) {
        try {
            log.info("Service: Starting UD AR reconciliation, settlement date: {}", settlementDate);
            int affectedRows = udArReconciliationRepository.insertUdArReconciliationResults(settlementDate);
            boolean result = affectedRows >= 0; // 0 rows is also considered success (no data)
            
            if (result) {
                log.info("Service: UD AR reconciliation succeeded, settlement date: {}", settlementDate);
            } else {
                log.warn("Service: UD AR reconciliation failed, settlement date: {}", settlementDate);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Service: Exception occurred during UD AR reconciliation, settlement date: {}", settlementDate, e);
            throw new RuntimeException("Failed to execute UD AR reconciliation: " + e.getMessage(), e);
        }
    }
}

