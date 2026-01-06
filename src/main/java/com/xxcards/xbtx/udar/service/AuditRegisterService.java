package com.xxcards.xbtx.udar.service;

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
                    log.error("Error processing transaction for deviceId: {}, seqNum: {}", 
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
            log.error("Unexpected error processing audit register request", e);
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
            log.warn("Device restart detected for deviceId: {}, beId: {}, businessDate: {}. " +
                    "Current AR seqNum: {}, Previous max seqNum exists. " +
                    "Will use accumulated counts from database.",
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
     * 檢查並記錄跨日期場景（前一天的未完成交易）
     * 例如：10-Dec 的交易在 11-Dec 發送
     */
    private void checkAndLogCrossDateScenario(AuditRegisterTransaction txn) {
        LocalDate businessDate = txn.getBusinessDate();
        LocalDate currentDate = LocalDate.now();
        OffsetDateTime transactionDateTime = txn.getTransactionDateTime();
        
        // 如果業務日期不是今天，可能是跨日期的未完成交易
        if (!businessDate.equals(currentDate)) {
            if (businessDate.isBefore(currentDate)) {
                log.info("Processing outstanding transactions from previous business date. " +
                        "DeviceId: {}, BusinessDate: {}, TransactionDateTime: {}, CurrentDate: {}. " +
                        "This may be outstanding transactions that were not uploaded before device shutdown.",
                        txn.getDeviceId(), businessDate, transactionDateTime, currentDate);
            } else {
                log.warn("Business date is in the future. DeviceId: {}, BusinessDate: {}, CurrentDate: {}",
                        txn.getDeviceId(), businessDate, currentDate);
            }
        }
        
        // 檢查交易時間和業務日期是否一致（允許一定範圍的差異）
        if (transactionDateTime != null) {
            LocalDate transactionDate = transactionDateTime.toLocalDate();
            // 如果交易日期和業務日期不一致，記錄警告
            if (!transactionDate.equals(businessDate) && 
                !transactionDate.equals(businessDate.minusDays(1)) && 
                !transactionDate.equals(businessDate.plusDays(1))) {
                log.warn("Transaction date and business date mismatch. " +
                        "DeviceId: {}, BusinessDate: {}, TransactionDate: {}. " +
                        "This may indicate data inconsistency.",
                        txn.getDeviceId(), businessDate, transactionDate);
            }
        }
    }
    
    /**
     * 檢查設備是否重啟（計數重置）
     * 如果當前的 auditRegisterSeqNum 小於之前記錄的最大值，說明設備重啟了
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
     * 更新設備審計註冊摘要
     * 處理設備重啟後計數重置的情況，確保累計計數正確
     * 重要：摘要按業務日期（businessDate）分組，因此跨日期發送的未完成交易
     * （例如 10-Dec 的交易在 11-Dec 發送）會正確累計到對應的業務日期
     */
    private void updateDeviceAuditRegisterSummary(AuditRegisterTransaction txn, boolean deviceRestarted) {
        LocalDate businessDate = txn.getBusinessDate();
        OffsetDateTime now = OffsetDateTime.now();
        LocalDate currentDate = LocalDate.now();
        boolean isCrossDate = !businessDate.equals(currentDate);
        
        for (AuditRegisterEntry entry : txn.getAuditRegisterEntries()) {
            String cardMediaTypeId = entry.getCardMediaTypeId() != null ? 
                    entry.getCardMediaTypeId() : "";
            
            // 查詢現有的摘要記錄（按業務日期查詢，確保跨日期交易正確累計）
            Optional<DeviceAuditRegisterSummary> existingSummary = 
                    deviceAuditRegisterSummaryRepository.findByDeviceIdAndBeIdAndBusinessDateAndArTypeIdentifierAndCardMediaTypeId(
                            txn.getDeviceId(), 
                            txn.getBeId(), 
                            businessDate,  // 使用業務日期，不是當前日期
                            entry.getArTypeIdentifier(), 
                            cardMediaTypeId);
            
            DeviceAuditRegisterSummary summary;
            
            if (existingSummary.isPresent()) {
                // 更新現有記錄
                summary = existingSummary.get();
                
                Long currentCount = entry.getCount() != null ? entry.getCount().longValue() : 0L;
                BigDecimal currentValue = entry.getValue() != null ? 
                        BigDecimal.valueOf(entry.getValue()) : BigDecimal.ZERO;
                
                // 累計計數和金額
                // 注意：即使設備重啟或跨日期發送，累計邏輯相同
                // 因為我們按業務日期分組，所以會正確累計到對應的業務日期
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
                    log.info("Updating cross-date summary for deviceId: {}, businessDate: {}, currentDate: {}. " +
                            "arType: {}, cardMediaType: {}. " +
                            "Previous total: count={}, value={}. Current: count={}, value={}. " +
                            "New total: count={}, value={}",
                            txn.getDeviceId(), businessDate, currentDate,
                            entry.getArTypeIdentifier(), cardMediaTypeId,
                            previousCount, previousValue,
                            currentCount, currentValue,
                            newTotalCount, newTotalValue);
                } else {
                    log.debug("Updated summary for deviceId: {}, arType: {}, cardMediaType: {}. " +
                            "Previous total: count={}, value={}. Current: count={}, value={}. " +
                            "New total: count={}, value={}",
                            txn.getDeviceId(), entry.getArTypeIdentifier(), cardMediaTypeId,
                            previousCount, previousValue,
                            currentCount, currentValue,
                            newTotalCount, newTotalValue);
                }
            } else {
                // 創建新記錄
                Long initialCount = entry.getCount() != null ? entry.getCount().longValue() : 0L;
                BigDecimal initialValue = entry.getValue() != null ? 
                        BigDecimal.valueOf(entry.getValue()) : BigDecimal.ZERO;
                
                summary = DeviceAuditRegisterSummary.builder()
                        .deviceId(txn.getDeviceId())
                        .beId(txn.getBeId())
                        .businessDate(businessDate)  // 使用業務日期，確保跨日期交易正確分組
                        .arTypeIdentifier(entry.getArTypeIdentifier())
                        .cardMediaTypeId(cardMediaTypeId)
                        .totalCount(initialCount)
                        .totalValue(initialValue)
                        .lastArSeqNum(txn.getAuditRegisterSeqNum())
                        .lastUpdatedTime(now)
                        .createdTime(now)
                        .build();
                
                if (isCrossDate) {
                    log.info("Creating cross-date summary for deviceId: {}, businessDate: {}, currentDate: {}. " +
                            "arType: {}, cardMediaType: {}. " +
                            "Initial: count={}, value={}",
                            txn.getDeviceId(), businessDate, currentDate,
                            entry.getArTypeIdentifier(), cardMediaTypeId,
                            initialCount, initialValue);
                } else {
                    log.debug("Created new summary for deviceId: {}, arType: {}, cardMediaType: {}. " +
                            "Initial: count={}, value={}",
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
            log.error("Failed to save to exception tables", e);
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

