package com.example.financial.service;

import com.example.financial.model.AuditRecord;
import com.example.financial.model.UsageData;
import com.example.financial.repository.AuditRecordRepository;
import com.example.financial.repository.UsageDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ReconciliationService {

    @Autowired
    private AuditRecordRepository auditRecordRepository;

    @Autowired
    private UsageDataRepository usageDataRepository;

    @Transactional
    public void performReconciliation() {
        List<AuditRecord> pendingRecords = auditRecordRepository.findByReconciliationStatus("PENDING");

        if (pendingRecords.isEmpty()) {
            log.info("No pending audit records found.");
            return;
        }

        log.info("Found {} pending audit records. Starting reconciliation...", pendingRecords.size());

        for (AuditRecord record : pendingRecords) {
            Optional<UsageData> usageDataOpt = usageDataRepository.findByDeviceIdAndDate(
                    record.getDeviceId(), record.getAuditDate());

            if (usageDataOpt.isPresent()) {
                UsageData usageData = usageDataOpt.get();
                boolean countsMatch = record.getTotalTransactions().equals(usageData.getTransactionCount());
                // Using compareTo for BigDecimal
                boolean amountsMatch = record.getTotalAmount().compareTo(usageData.getTotalAmount()) == 0;

                if (countsMatch && amountsMatch) {
                    record.setReconciliationStatus("MATCHED");
                    log.info("Reconciliation MATCHED for Device: {}, Date: {}", record.getDeviceId(), record.getAuditDate());
                } else {
                    record.setReconciliationStatus("MISMATCHED");
                    log.warn("Reconciliation MISMATCHED for Device: {}, Date: {}. Audit: [Count={}, Amount={}], Usage: [Count={}, Amount={}]",
                            record.getDeviceId(), record.getAuditDate(),
                            record.getTotalTransactions(), record.getTotalAmount(),
                            usageData.getTransactionCount(), usageData.getTotalAmount());
                }
            } else {
                record.setReconciliationStatus("MISSING_USAGE_DATA");
                log.warn("No Usage Data found for Device: {}, Date: {}", record.getDeviceId(), record.getAuditDate());
            }
            auditRecordRepository.save(record);
        }
    }
}
