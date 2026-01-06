package com.financial.recon.service;

import com.financial.recon.entity.TransactionTotal;
import com.financial.recon.repository.TransactionTotalSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionTotalSummaryServiceImpl implements TransactionTotalSummaryService {

    private final TransactionTotalSummaryRepository transactionTotalSummaryRepository;

    @Override
    @Transactional
    public boolean calculateTransactionTotal(LocalDate settlementDate) {
        try {
            log.info("Service: Starting transaction total calculation, settlement date: {}", settlementDate);
            List<TransactionTotalSummaryRepository.TransactionTotalAggregate> aggregates =
                    transactionTotalSummaryRepository.findTransactionTotalAggregates(settlementDate);

            if (aggregates.isEmpty()) {
                log.warn("Service: No transaction totals found for settlement date: {}", settlementDate);
                return true; // No data is considered success
            }

            // Convert aggregates to Entity objects
            List<TransactionTotal> transactionTotals = aggregates.stream()
                    .map(agg -> TransactionTotal.builder()
                            .settlementDate(agg.getSettlementDate())
                            .txnType(agg.getTxnType())
                            .txnSubtype(agg.getTxnSubtype())
                            .beId(agg.getBeId())
                            .debtorBeId(agg.getDebtorBeId())
                            .creditorBeId(agg.getCreditorBeId())
                            .issuerId(agg.getIssuerId())
                            .deviceId(agg.getDeviceId())
                            .udSettleCount(agg.getUdSettleCount())
                            .udSettleAmount(agg.getUdSettleAmount())
                            .udNotSettleCount(agg.getUdNotSettleCount())
                            .udNotSettleAmount(agg.getUdNotSettleAmount())
                            .beBusinessDate(agg.getBeBusinessDate())
                            .productCode(agg.getProductCode())
                            .apportionmentValue(agg.getApportionmentValue())
                            .build())
                    .collect(Collectors.toList());

            // Batch insert using saveAll
            List<TransactionTotal> saved = transactionTotalSummaryRepository.saveAll(transactionTotals);
            int totalInserted = saved.size();

            boolean result = totalInserted == aggregates.size();
            
            if (result) {
                log.info("Service: Transaction total calculation succeeded, settlement date: {}, inserted rows: {}", settlementDate, totalInserted);
            } else {
                log.warn("Service: Transaction total calculation partially failed, settlement date: {}, expected: {}, inserted: {}",
                        settlementDate, aggregates.size(), totalInserted);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Service: Exception occurred during transaction total calculation, settlement date: {}", settlementDate, e);
            throw new RuntimeException("Failed to calculate transaction total: " + e.getMessage(), e);
        }
    }
}

