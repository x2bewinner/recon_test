package com.xxcards.xbtx.udar.util;

import com.xxcards.xbtx.udar.dto.AuditRegisterEntry;
import com.xxcards.xbtx.udar.dto.AuditRegisterRequest;
import com.xxcards.xbtx.udar.dto.AuditRegisterTransaction;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 測試資料構建器
 * 用於創建測試用的請求物件
 */
public class TestDataBuilder {

    /**
     * 創建基本的審計註冊請求
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
     * 創建基本的交易物件
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
     * 創建基本的條目物件
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
     * 創建設備重啟場景的請求（seqNum 小於之前的值）
     */
    public static AuditRegisterRequest createDeviceRestartRequest() {
        AuditRegisterRequest request = new AuditRegisterRequest();
        List<AuditRegisterTransaction> transactions = new ArrayList<>();
        
        AuditRegisterTransaction txn = createBasicTransaction();
        txn.setAuditRegisterSeqNum(1); // 設備重啟後序列號重置為 1
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
     * 創建跨日期交易請求（業務日期是昨天）
     */
    public static AuditRegisterRequest createCrossDateRequest() {
        AuditRegisterRequest request = new AuditRegisterRequest();
        List<AuditRegisterTransaction> transactions = new ArrayList<>();
        
        AuditRegisterTransaction txn = createBasicTransaction();
        txn.setBusinessDate(LocalDate.now().minusDays(1)); // 業務日期是昨天
        txn.setTransactionDateTime(OffsetDateTime.now()); // 但發送時間是今天
        
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
     * 創建批次處理請求（多筆交易）
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
     * 創建多條目交易請求
     */
    public static AuditRegisterRequest createMultiEntryRequest() {
        AuditRegisterRequest request = new AuditRegisterRequest();
        List<AuditRegisterTransaction> transactions = new ArrayList<>();
        
        AuditRegisterTransaction txn = createBasicTransaction();
        
        List<AuditRegisterEntry> entries = new ArrayList<>();
        
        // 第一個條目
        AuditRegisterEntry entry1 = createBasicEntry();
        entry1.setArTypeIdentifier("AR-TYPE-001");
        entry1.setCardMediaTypeId("CARD-001");
        entry1.setCount(10);
        entry1.setValue(1000.00);
        entries.add(entry1);
        
        // 第二個條目
        AuditRegisterEntry entry2 = createBasicEntry();
        entry2.setArTypeIdentifier("AR-TYPE-002");
        entry2.setCardMediaTypeId("CARD-002");
        entry2.setCount(5);
        entry2.setValue(500.00);
        entries.add(entry2);
        
        // 第三個條目
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
     * 創建驗證失敗的請求（缺少必填欄位）
     */
    public static AuditRegisterRequest createInvalidRequest() {
        AuditRegisterRequest request = new AuditRegisterRequest();
        List<AuditRegisterTransaction> transactions = new ArrayList<>();
        
        AuditRegisterTransaction txn = new AuditRegisterTransaction();
        // 故意不設置必填欄位
        // txn.setTransactionType(null);
        // txn.setDeviceId(null);
        // txn.setBeId(null);
        
        transactions.add(txn);
        request.setAuditRegisterTxns(transactions);
        return request;
    }

    /**
     * 創建空條目的請求（用於測試驗證）
     */
    public static AuditRegisterRequest createEmptyEntriesRequest() {
        AuditRegisterRequest request = new AuditRegisterRequest();
        List<AuditRegisterTransaction> transactions = new ArrayList<>();
        
        AuditRegisterTransaction txn = createBasicTransaction();
        txn.setAuditRegisterEntries(new ArrayList<>()); // 空條目列表
        
        transactions.add(txn);
        request.setAuditRegisterTxns(transactions);
        return request;
    }

    /**
     * 創建特定設備和業務日期的請求
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

