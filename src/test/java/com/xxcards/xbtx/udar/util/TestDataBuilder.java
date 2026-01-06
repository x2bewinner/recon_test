package com.xxcards.xbtx.udar.util;

import com.xxcards.xbtx.udar.dto.AuditRegisterEntry;
import com.xxcards.xbtx.udar.dto.AuditRegisterRequest;
import com.xxcards.xbtx.udar.dto.AuditRegisterTransaction;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Test data builder
 * Used to create test request objects
 */
public class TestDataBuilder {

    /**
     * Create basic audit register request
     */
    public static AuditRegisterRequest createBasicRequest() {
        AuditRegisterRequest request = new AuditRegisterRequest();
        List<AuditRegisterTransaction> transactions = new ArrayList<>();
        
        AuditRegisterTransaction txn = createBasicTransaction();
        transactions.add(txn);
        
        request.setAuditRegisterTxns(transactions);
        return request;
    }

    /**
     * Create basic transaction object
     */
    public static AuditRegisterTransaction createBasicTransaction() {
        AuditRegisterTransaction txn = new AuditRegisterTransaction();
        txn.setTransactionType("AUD001");
        txn.setTransactionDateTime(OffsetDateTime.now());
        txn.setDeviceId("DEVICE-001");
        txn.setEquipmentId("EQ-001");
        txn.setBeId(1);
        txn.setAuditRegisterSeqNum(1);
        txn.setBusinessDate(LocalDate.now());
        txn.setDeviceTypeId("TYPE-001");
        txn.setDeviceSpecialMode("NORMAL");
        txn.setServiceId("SERVICE-001");
        
        List<AuditRegisterEntry> entries = new ArrayList<>();
        AuditRegisterEntry entry = createBasicEntry();
        entries.add(entry);
        
        txn.setAuditRegisterEntries(entries);
        return txn;
    }

    /**
     * Create basic entry object
     */
    public static AuditRegisterEntry createBasicEntry() {
        AuditRegisterEntry entry = new AuditRegisterEntry();
        entry.setArTypeIdentifier("AR-TYPE-001");
        entry.setCardMediaTypeId("CARD-001");
        entry.setCount(10);
        entry.setValue(1000.50);
        return entry;
    }

    /**
     * Create device restart scenario request (seqNum less than previous value)
     */
    public static AuditRegisterRequest createDeviceRestartRequest() {
        AuditRegisterRequest request = new AuditRegisterRequest();
        List<AuditRegisterTransaction> transactions = new ArrayList<>();
        
        AuditRegisterTransaction txn = createBasicTransaction();
        txn.setAuditRegisterSeqNum(1); // Sequence number reset to 1 after device restart
        txn.setBusinessDate(LocalDate.now());
        
        List<AuditRegisterEntry> entries = new ArrayList<>();
        AuditRegisterEntry entry = createBasicEntry();
        entry.setCount(5);
        entry.setValue(500.00);
        entries.add(entry);
        
        txn.setAuditRegisterEntries(entries);
        transactions.add(txn);
        
        request.setAuditRegisterTxns(transactions);
        return request;
    }

    /**
     * Create cross-date transaction request (business date is yesterday)
     */
    public static AuditRegisterRequest createCrossDateRequest() {
        AuditRegisterRequest request = new AuditRegisterRequest();
        List<AuditRegisterTransaction> transactions = new ArrayList<>();
        
        AuditRegisterTransaction txn = createBasicTransaction();
        txn.setBusinessDate(LocalDate.now().minusDays(1)); // Business date is yesterday
        txn.setTransactionDateTime(OffsetDateTime.now()); // but send time is today
        
        List<AuditRegisterEntry> entries = new ArrayList<>();
        AuditRegisterEntry entry = createBasicEntry();
        entry.setCount(5);
        entry.setValue(500.00);
        entries.add(entry);
        
        txn.setAuditRegisterEntries(entries);
        transactions.add(txn);
        
        request.setAuditRegisterTxns(transactions);
        return request;
    }

    /**
     * Create batch processing request (multiple transactions)
     */
    public static AuditRegisterRequest createBatchRequest(int transactionCount) {
        AuditRegisterRequest request = new AuditRegisterRequest();
        List<AuditRegisterTransaction> transactions = new ArrayList<>();
        
        for (int i = 1; i <= transactionCount; i++) {
            AuditRegisterTransaction txn = createBasicTransaction();
            txn.setDeviceId("DEVICE-" + String.format("%03d", i));
            txn.setAuditRegisterSeqNum(i);
            transactions.add(txn);
        }
        
        request.setAuditRegisterTxns(transactions);
        return request;
    }

    /**
     * Create multi-entry transaction request
     */
    public static AuditRegisterRequest createMultiEntryRequest() {
        AuditRegisterRequest request = new AuditRegisterRequest();
        List<AuditRegisterTransaction> transactions = new ArrayList<>();
        
        AuditRegisterTransaction txn = createBasicTransaction();
        
        List<AuditRegisterEntry> entries = new ArrayList<>();
        
        // First entry
        AuditRegisterEntry entry1 = createBasicEntry();
        entry1.setArTypeIdentifier("AR-TYPE-001");
        entry1.setCardMediaTypeId("CARD-001");
        entry1.setCount(10);
        entry1.setValue(1000.00);
        entries.add(entry1);
        
        // Second entry
        AuditRegisterEntry entry2 = createBasicEntry();
        entry2.setArTypeIdentifier("AR-TYPE-002");
        entry2.setCardMediaTypeId("CARD-002");
        entry2.setCount(5);
        entry2.setValue(500.00);
        entries.add(entry2);
        
        // Third entry
        AuditRegisterEntry entry3 = createBasicEntry();
        entry3.setArTypeIdentifier("AR-TYPE-001");
        entry3.setCardMediaTypeId("CARD-003");
        entry3.setCount(3);
        entry3.setValue(300.00);
        entries.add(entry3);
        
        txn.setAuditRegisterEntries(entries);
        transactions.add(txn);
        
        request.setAuditRegisterTxns(transactions);
        return request;
    }

    /**
     * Create validation failed request (missing required fields)
     */
    public static AuditRegisterRequest createInvalidRequest() {
        AuditRegisterRequest request = new AuditRegisterRequest();
        List<AuditRegisterTransaction> transactions = new ArrayList<>();
        
        AuditRegisterTransaction txn = new AuditRegisterTransaction();
        // Intentionally not set required fields
        // txn.setTransactionType(null);
        // txn.setDeviceId(null);
        // txn.setBeId(null);
        
        transactions.add(txn);
        request.setAuditRegisterTxns(transactions);
        return request;
    }

    /**
     * Create empty entries request (for testing validation)
     */
    public static AuditRegisterRequest createEmptyEntriesRequest() {
        AuditRegisterRequest request = new AuditRegisterRequest();
        List<AuditRegisterTransaction> transactions = new ArrayList<>();
        
        AuditRegisterTransaction txn = createBasicTransaction();
        txn.setAuditRegisterEntries(new ArrayList<>()); // Empty entries list
        
        transactions.add(txn);
        request.setAuditRegisterTxns(transactions);
        return request;
    }

    /**
     * Create request for specific device and business date
     */
    public static AuditRegisterRequest createRequestForDeviceAndDate(
            String deviceId, LocalDate businessDate, Integer seqNum) {
        AuditRegisterRequest request = new AuditRegisterRequest();
        List<AuditRegisterTransaction> transactions = new ArrayList<>();
        
        AuditRegisterTransaction txn = createBasicTransaction();
        txn.setDeviceId(deviceId);
        txn.setBusinessDate(businessDate);
        txn.setAuditRegisterSeqNum(seqNum);
        
        transactions.add(txn);
        request.setAuditRegisterTxns(transactions);
        return request;
    }
}

