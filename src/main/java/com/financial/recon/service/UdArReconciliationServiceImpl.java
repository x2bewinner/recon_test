package com.financial.recon.service;

import com.financial.recon.entity.UdArReconciliation;
import com.financial.recon.repository.UdArReconciliationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UdArReconciliationServiceImpl implements UdArReconciliationService {

    private final UdArReconciliationRepository udArReconciliationRepository;

    @Override
    @Transactional
    public boolean executeReconciliation(LocalDate settlementDate) {
        try {
            log.info("Service: Starting UD AR reconciliation, settlement date: {}", settlementDate);
            List<UdArReconciliationRepository.UdArReconciliationAggregate> aggregates =
                    udArReconciliationRepository.findUdArReconciliationAggregates(settlementDate);

            if (aggregates.isEmpty()) {
                log.warn("Service: No UD AR reconciliation data found for settlement date: {}", settlementDate);
                return true; // No data is considered success
            }

            // Convert aggregates to Entity objects
            OffsetDateTime now = OffsetDateTime.now();
            List<UdArReconciliation> reconciliations = aggregates.stream()
                    .map(agg -> UdArReconciliation.builder()
                            .beId(agg.getBeId())
                            .settlementDate(settlementDate)
                            .arTypeIdentifier(agg.getArTypeIdentifier())
                            .cardMediaTypeId(agg.getCardMediaTypeId())
                            .transactionCount(agg.getTransactionCount())
                            .totalCount(agg.getTotalCount())
                            .totalValue(agg.getTotalValue())
                            .deviceCount(agg.getDeviceCount())
                            .reconciliationStatus("SUCCESS")
                            .createdTime(now)
                            .lastUpdatedTime(now)
                            .build())
                    .collect(Collectors.toList());

            // Batch insert using saveAll
            List<UdArReconciliation> saved = udArReconciliationRepository.saveAll(reconciliations);
            int totalInserted = saved.size();

            boolean result = totalInserted == aggregates.size();
            
            if (result) {
                log.info("Service: UD AR reconciliation succeeded, settlement date: {}, inserted rows: {}", settlementDate, totalInserted);
            } else {
                log.warn("Service: UD AR reconciliation partially failed, settlement date: {}, expected: {}, inserted: {}",
                        settlementDate, aggregates.size(), totalInserted);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Service: Exception occurred during UD AR reconciliation, settlement date: {}", settlementDate, e);
            throw new RuntimeException("Failed to execute UD AR reconciliation: " + e.getMessage(), e);
        }
    }
}

