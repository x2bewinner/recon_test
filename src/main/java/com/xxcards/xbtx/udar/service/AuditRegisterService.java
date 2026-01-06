package com.xxcards.xbtx.udar.service;

import com.xxcards.xbtx.udar.constant.LogMessage;
import com.xxcards.xbtx.udar.dto.AuditRegisterEntry;
import com.xxcards.xbtx.udar.dto.AuditRegisterRequest;
import com.xxcards.xbtx.udar.dto.AuditRegisterResponse;
import com.xxcards.xbtx.udar.dto.AuditRegisterTransaction;
import com.xxcards.xbtx.udar.entity.DeviceAuditRegisterSummary;
import com.xxcards.xbtx.udar.entity.MirrorAr;
import com.xxcards.xbtx.udar.entity.MirrorArDetail;
import com.xxcards.xbtx.udar.entity.MirrorArDetailEx;
import com.xxcards.xbtx.udar.entity.MirrorArEx;
import com.xxcards.xbtx.udar.repository.DeviceAuditRegisterSummaryRepository;
import com.xxcards.xbtx.udar.repository.MirrorArDetailExRepository;
import com.xxcards.xbtx.udar.repository.MirrorArDetailRepository;
import com.xxcards.xbtx.udar.repository.MirrorArExRepository;
import com.xxcards.xbtx.udar.repository.MirrorArRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditRegisterService {

    private final MirrorArRepository mirrorArRepository;
    private final MirrorArExRepository mirrorArExRepository;
    private final MirrorArDetailRepository mirrorArDetailRepository;
    private final MirrorArDetailExRepository mirrorArDetailExRepository;
    private final DeviceAuditRegisterSummaryRepository deviceAuditRegisterSummaryRepository;

    @Transactional
    public AuditRegisterResponse processAuditRegister(AuditRegisterRequest request, String clientRequestId) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        try {
            for (AuditRegisterTransaction txn : request.getAuditRegisterTxns()) {
                try {
                    processTransaction(txn, clientRequestId);
                    successCount++;
                } catch (Exception e) {
                    log.error(LogMessage.E_SERVICE_AUDIT_REGISTER_TRANSACTION_ERROR.getMessage(), 
                            txn.getDeviceId(), txn.getAuditRegisterSeqNum(), e);
                    errors.add(String.format("Transaction processing failed for deviceId: %s, seqNum: %d - %s",
                            txn.getDeviceId(), txn.getAuditRegisterSeqNum(), e.getMessage()));
                    failureCount++;
                    
                    // Save to exception tables
                    saveToExceptionTables(txn, clientRequestId, e.getMessage());
                }
            }

            if (errors.isEmpty()) {
                return AuditRegisterResponse.builder()
                        .responseCode("SUCCESS")
                        .responseMessage(String.format("Successfully processed %d transaction(s)", successCount))
                        .errors(null)
                        .build();
            } else {
                return AuditRegisterResponse.builder()
                        .responseCode("PARTIAL_SUCCESS")
                        .responseMessage(String.format("Processed %d success, %d failure(s)", successCount, failureCount))
                        .errors(errors)
                        .build();
            }
        } catch (Exception e) {
            log.error(LogMessage.E_SERVICE_AUDIT_REGISTER_UNEXPECTED_ERROR.getMessage(), e);
            return AuditRegisterResponse.builder()
                    .responseCode("ERROR")
                    .responseMessage("Failed to process audit register request: " + e.getMessage())
                    .errors(List.of(e.getMessage()))
                    .build();
        }
    }

    private void processTransaction(AuditRegisterTransaction txn, String clientRequestId) {
        String referenceId = generateReferenceId();
        String refRecordId = "001"; // Default value, can be configured
        
        // Check for cross-date scenario (outstanding transactions from previous day)
        checkAndLogCrossDateScenario(txn);
        
        // Check if device was restarted (count reset)
        boolean deviceRestarted = checkDeviceRestart(txn);
        
        if (deviceRestarted) {
            log.warn(LogMessage.SERVICE_DEVICE_RESTART_DETECTED.getMessage(),
                    txn.getDeviceId(), txn.getBeId(), txn.getBusinessDate(), 
                    txn.getAuditRegisterSeqNum());
        }
        
        // Process and update device audit register summary for reconciliation
        // Note: Summary is tracked by businessDate, so outstanding transactions from 10-Dec
        // will be correctly accumulated to 10-Dec summary even if sent on 11-Dec
        updateDeviceAuditRegisterSummary(txn, deviceRestarted);
        
        // Create MirrorAr entity
        MirrorAr mirrorAr = buildMirrorAr(txn, referenceId, refRecordId, clientRequestId);
        mirrorArRepository.save(mirrorAr);

        // Create MirrorArDetail entities
        int entryIndex = 1;
        for (AuditRegisterEntry entry : txn.getAuditRegisterEntries()) {
            MirrorArDetail detail = buildMirrorArDetail(txn, entry, referenceId, refRecordId, entryIndex);
            mirrorArDetailRepository.save(detail);
            entryIndex++;
        }
    }
    
    /**
     * Check and log cross-date scenario (outstanding transactions from previous day)
     * Example: Transactions from 10-Dec sent on 11-Dec
     */
    private void checkAndLogCrossDateScenario(AuditRegisterTransaction txn) {
        LocalDate businessDate = txn.getBusinessDate();
        LocalDate currentDate = LocalDate.now();
        OffsetDateTime transactionDateTime = txn.getTransactionDateTime();
        
        // If business date is not today, it may be outstanding transactions from a previous date
        if (!businessDate.equals(currentDate)) {
            if (businessDate.isBefore(currentDate)) {
                log.info(LogMessage.I_SERVICE_PROCESSING_OUTSTANDING_TRANSACTIONS.getMessage(),
                        txn.getDeviceId(), businessDate, transactionDateTime, currentDate);
            } else {
                log.warn(LogMessage.SERVICE_BUSINESS_DATE_IN_FUTURE.getMessage(),
                        txn.getDeviceId(), businessDate, currentDate);
            }
        }
        
        // Check if transaction time and business date are consistent (allow certain range of differences)
        if (transactionDateTime != null) {
            LocalDate transactionDate = transactionDateTime.toLocalDate();
            // If transaction date and business date are inconsistent, log warning
            if (!transactionDate.equals(businessDate) && 
                !transactionDate.equals(businessDate.minusDays(1)) && 
                !transactionDate.equals(businessDate.plusDays(1))) {
                log.warn(LogMessage.SERVICE_DATE_MISMATCH.getMessage(),
                        txn.getDeviceId(), businessDate, transactionDate);
            }
        }
    }
    
    /**
     * Check if device was restarted (count reset)
     * If current auditRegisterSeqNum is less than previously recorded maximum, device was restarted
     */
    private boolean checkDeviceRestart(AuditRegisterTransaction txn) {
        Optional<Integer> maxSeqNum = deviceAuditRegisterSummaryRepository
                .findMaxArSeqNumByDeviceAndDate(
                        txn.getDeviceId(), 
                        txn.getBeId(), 
                        txn.getBusinessDate());
        
        if (maxSeqNum.isPresent() && txn.getAuditRegisterSeqNum() < maxSeqNum.get()) {
            return true;
        }
        return false;
    }
    
    /**
     * Update device audit register summary
     * Handle device restart count reset scenario, ensure accumulated counts are correct
     * Important: Summary is grouped by businessDate, so outstanding transactions sent across dates
     * (e.g., transactions from 10-Dec sent on 11-Dec) will be correctly accumulated to the corresponding business date
     */
    private void updateDeviceAuditRegisterSummary(AuditRegisterTransaction txn, boolean deviceRestarted) {
        LocalDate businessDate = txn.getBusinessDate();
        OffsetDateTime now = OffsetDateTime.now();
        LocalDate currentDate = LocalDate.now();
        boolean isCrossDate = !businessDate.equals(currentDate);
        
        for (AuditRegisterEntry entry : txn.getAuditRegisterEntries()) {
            String cardMediaTypeId = entry.getCardMediaTypeId() != null ? 
                    entry.getCardMediaTypeId() : "";
            
            // Query existing summary record (query by business date to ensure cross-date transactions are correctly accumulated)
            Optional<DeviceAuditRegisterSummary> existingSummary = 
                    deviceAuditRegisterSummaryRepository.findByDeviceIdAndBeIdAndBusinessDateAndArTypeIdentifierAndCardMediaTypeId(
                            txn.getDeviceId(), 
                            txn.getBeId(), 
                            businessDate,  // Use business date, not current date
                            entry.getArTypeIdentifier(), 
                            cardMediaTypeId);
            
            DeviceAuditRegisterSummary summary;
            
            if (existingSummary.isPresent()) {
                // Update existing record
                summary = existingSummary.get();
                
                Long currentCount = entry.getCount() != null ? entry.getCount().longValue() : 0L;
                BigDecimal currentValue = entry.getValue() != null ? 
                        BigDecimal.valueOf(entry.getValue()) : BigDecimal.ZERO;
                
                // Accumulate count and amount
                // Note: Even if device restarted or sent across dates, accumulation logic is the same
                // Because we group by business date, it will correctly accumulate to the corresponding business date
                Long previousCount = summary.getTotalCount();
                BigDecimal previousValue = summary.getTotalValue() != null ? 
                        summary.getTotalValue() : BigDecimal.ZERO;
                
                Long newTotalCount = previousCount + currentCount;
                BigDecimal newTotalValue = previousValue.add(currentValue);
                
                summary.setTotalCount(newTotalCount);
                summary.setTotalValue(newTotalValue);
                summary.setLastArSeqNum(txn.getAuditRegisterSeqNum());
                summary.setLastUpdatedTime(now);
                
                if (isCrossDate) {
                    log.info(LogMessage.I_SERVICE_UPDATE_CROSS_DATE_SUMMARY.getMessage(),
                            txn.getDeviceId(), businessDate, currentDate,
                            entry.getArTypeIdentifier(), cardMediaTypeId,
                            previousCount, previousValue,
                            currentCount, currentValue,
                            newTotalCount, newTotalValue);
                } else {
                    log.debug(LogMessage.SERVICE_UPDATE_SUMMARY_DEBUG.getMessage(),
                            txn.getDeviceId(), entry.getArTypeIdentifier(), cardMediaTypeId,
                            previousCount, previousValue,
                            currentCount, currentValue,
                            newTotalCount, newTotalValue);
                }
            } else {
                // Create new record
                Long initialCount = entry.getCount() != null ? entry.getCount().longValue() : 0L;
                BigDecimal initialValue = entry.getValue() != null ? 
                        BigDecimal.valueOf(entry.getValue()) : BigDecimal.ZERO;
                
                summary = DeviceAuditRegisterSummary.builder()
                        .deviceId(txn.getDeviceId())
                        .beId(txn.getBeId())
                        .businessDate(businessDate)  // Use business date to ensure cross-date transactions are correctly grouped
                        .arTypeIdentifier(entry.getArTypeIdentifier())
                        .cardMediaTypeId(cardMediaTypeId)
                        .totalCount(initialCount)
                        .totalValue(initialValue)
                        .lastArSeqNum(txn.getAuditRegisterSeqNum())
                        .lastUpdatedTime(now)
                        .createdTime(now)
                        .build();
                
                if (isCrossDate) {
                    log.info(LogMessage.I_SERVICE_CREATE_CROSS_DATE_SUMMARY.getMessage(),
                            txn.getDeviceId(), businessDate, currentDate,
                            entry.getArTypeIdentifier(), cardMediaTypeId,
                            initialCount, initialValue);
                } else {
                    log.debug(LogMessage.SERVICE_CREATE_SUMMARY_DEBUG.getMessage(),
                            txn.getDeviceId(), entry.getArTypeIdentifier(), cardMediaTypeId,
                            initialCount, initialValue);
                }
            }
            
            deviceAuditRegisterSummaryRepository.save(summary);
        }
    }

    private MirrorAr buildMirrorAr(AuditRegisterTransaction txn, String referenceId, 
                                   String refRecordId, String clientRequestId) {
        OffsetDateTime now = OffsetDateTime.now();
        
        return MirrorAr.builder()
                .referenceId(referenceId)
                .refRecordId(refRecordId)
                .refTotalCount(txn.getAuditRegisterEntries().size())
                .txnType(extractTxnType(txn.getTransactionType()))
                .txnSubtype(extractTxnSubtype(txn.getTransactionType()))
                .endTxnTime(txn.getTransactionDateTime())
                .udsn(generateUdsn(txn))
                .deviceId(txn.getDeviceId())
                .hardwareType(txn.getDeviceTypeId())
                .serviceMode(txn.getDeviceSpecialMode())
                .beId(txn.getBeId())
                .auditRegisterSeqNum(txn.getAuditRegisterSeqNum())
                .businessDate(txn.getBusinessDate() != null ? 
                        txn.getBusinessDate().atStartOfDay().atOffset(OffsetDateTime.now().getOffset()) : null)
                .settlementDate(now) // Set to current time, can be configured based on business rules
                .receivedTime(now)
                .lastUpdatedTime(now)
                .physicalDeviceId(txn.getEquipmentId())
                .build();
    }

    private MirrorArDetail buildMirrorArDetail(AuditRegisterTransaction txn, AuditRegisterEntry entry,
                                              String referenceId, String refRecordId, int entryIndex) {
        OffsetDateTime now = OffsetDateTime.now();
        String arEntryId = String.format("%03d", entryIndex);
        
        return MirrorArDetail.builder()
                .referenceId(referenceId)
                .refRecordId(refRecordId)
                .arEntryId(arEntryId)
                .arId(entry.getArTypeIdentifier())
                .idType(parseIdType(entry.getCardMediaTypeId()))
                .count(entry.getCount() != null ? entry.getCount().longValue() : null)
                .value(entry.getValue() != null ? BigDecimal.valueOf(entry.getValue()) : null)
                .lastUpdatedTime(now)
                .build();
    }

    private void saveToExceptionTables(AuditRegisterTransaction txn, String clientRequestId, String errorMessage) {
        try {
            String referenceId = generateReferenceId();
            String refRecordId = "001";
            OffsetDateTime now = OffsetDateTime.now();

            // Save to MIRROR_AR_EX
            MirrorArEx mirrorArEx = MirrorArEx.builder()
                    .referenceId(referenceId)
                    .refRecordId(refRecordId)
                    .refTotalCount(txn.getAuditRegisterEntries().size())
                    .txnType(extractTxnType(txn.getTransactionType()))
                    .txnSubtype(extractTxnSubtype(txn.getTransactionType()))
                    .endTxnTime(txn.getTransactionDateTime())
                    .udsn(generateUdsn(txn))
                    .deviceId(txn.getDeviceId())
                    .hardwareType(txn.getDeviceTypeId())
                    .serviceMode(txn.getDeviceSpecialMode())
                    .beId(txn.getBeId())
                    .auditRegisterSeqNum(txn.getAuditRegisterSeqNum())
                    .businessDate(txn.getBusinessDate() != null ? 
                            txn.getBusinessDate().atStartOfDay().atOffset(OffsetDateTime.now().getOffset()) : null)
                    .settlementDate(now)
                    .receivedTime(now)
                    .lastUpdatedTime(now)
                    .physicalDeviceId(txn.getEquipmentId())
                    .build();
            mirrorArExRepository.save(mirrorArEx);

            // Save to MIRROR_AR_DETAIL_EX
            int entryIndex = 1;
            for (AuditRegisterEntry entry : txn.getAuditRegisterEntries()) {
                MirrorArDetailEx detailEx = MirrorArDetailEx.builder()
                        .referenceId(referenceId)
                        .refRecordId(refRecordId)
                        .arEntryId(String.format("%03d", entryIndex))
                        .arId(entry.getArTypeIdentifier())
                        .idType(entry.getCardMediaTypeId())
                        .count(entry.getCount() != null ? entry.getCount().longValue() : null)
                        .value(entry.getValue() != null ? BigDecimal.valueOf(entry.getValue()) : null)
                        .lastUpdatedTime(now)
                        .build();
                mirrorArDetailExRepository.save(detailEx);
                entryIndex++;
            }
        } catch (Exception e) {
            log.error(LogMessage.E_SERVICE_SAVE_TO_EXCEPTION_TABLES_ERROR.getMessage(), e);
        }
    }

    private String generateReferenceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 32).toUpperCase();
    }

    private String generateUdsn(AuditRegisterTransaction txn) {
        // Generate UDSN based on device and sequence number
        return String.format("%s-%d-%d", txn.getDeviceId(), txn.getAuditRegisterSeqNum(), 
                System.currentTimeMillis());
    }

    private String extractTxnType(String transactionType) {
        // Extract transaction type (first 3 characters or default)
        if (transactionType != null && transactionType.length() >= 3) {
            return transactionType.substring(0, 3);
        }
        return transactionType != null ? transactionType : "UNK";
    }

    private String extractTxnSubtype(String transactionType) {
        // Extract transaction subtype (next 3 characters or default)
        if (transactionType != null && transactionType.length() >= 6) {
            return transactionType.substring(3, 6);
        }
        return "000";
    }

    private Integer parseIdType(String cardMediaTypeId) {
        // Parse card media type ID to integer
        if (cardMediaTypeId == null || cardMediaTypeId.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(cardMediaTypeId);
        } catch (NumberFormatException e) {
            // If not numeric, return hash code or default
            return Math.abs(cardMediaTypeId.hashCode() % 1000000);
        }
    }
}

