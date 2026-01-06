package com.xxcards.xbtx.udar.constant;

/**
 * 日誌訊息枚舉類
 * 統一管理應用程式中的所有日誌訊息模板
 */
public enum LogMessage {

    // ==================== Controller 日誌訊息 ====================
    
    /**
     * 收到審計註冊請求
     * 參數: clientRequestId, transactionCount
     */
    I_CONTROLLER_AUDIT_REGISTER_RECEIVED("Received audit register request. ClientRequestId: {}, TransactionCount: {}"),
    
    /**
     * 結算日期未提供，使用當前日期
     * 參數: settlementDate
     */
    I_CONTROLLER_SETTLEMENT_DATE_NOT_PROVIDED("Settlement date not provided, using current date: {}"),
    
    /**
     * 開始觸發交易總額計算批次作業
     * 參數: settlementDate
     */
    I_CONTROLLER_TRIGGER_TRANSACTION_TOTAL_CALCULATION("Starting to trigger transaction total calculation batch job, settlement date: {}"),
    
    /**
     * 開始觸發 UD AR 對帳批次作業
     * 參數: settlementDate
     */
    I_CONTROLLER_TRIGGER_UD_AR_RECONCILIATION("Starting to trigger UD AR reconciliation batch job, settlement date: {}"),
    
    /**
     * 批次作業啟動成功
     * 參數: settlementDate
     */
    I_CONTROLLER_BATCH_JOB_STARTED_SUCCESS("Batch job started successfully, settlement date: {}"),
    
    /**
     * UD AR 對帳批次作業啟動成功
     * 參數: settlementDate
     */
    I_CONTROLLER_UD_AR_BATCH_JOB_STARTED_SUCCESS("UD AR reconciliation batch job started successfully, settlement date: {}"),
    
    /**
     * 啟動批次作業時發生異常
     * 參數: settlementDate
     */
    E_CONTROLLER_BATCH_JOB_START_ERROR("Exception occurred while starting batch job, settlement date: {}"),
    
    /**
     * 啟動 UD AR 對帳批次作業時發生異常
     * 參數: settlementDate
     */
    E_CONTROLLER_UD_AR_BATCH_JOB_START_ERROR("Exception occurred while starting UD AR reconciliation batch job, settlement date: {}"),

    // ==================== Service 日誌訊息 ====================
    
    /**
     * 開始交易總額計算
     * 參數: settlementDate
     */
    I_SERVICE_TRANSACTION_TOTAL_START("Service: Starting transaction total calculation, settlement date: {}"),
    
    /**
     * 未找到交易總額數據
     * 參數: settlementDate
     */
    SERVICE_TRANSACTION_TOTAL_NOT_FOUND("Service: No transaction totals found for settlement date: {}"),
    
    /**
     * 交易總額計算成功
     * 參數: settlementDate, insertedRows
     */
    I_SERVICE_TRANSACTION_TOTAL_SUCCESS("Service: Transaction total calculation succeeded, settlement date: {}, inserted rows: {}"),
    
    /**
     * 交易總額計算部分失敗
     * 參數: settlementDate, expected, inserted
     */
    SERVICE_TRANSACTION_TOTAL_PARTIAL_FAIL("Service: Transaction total calculation partially failed, settlement date: {}, expected: {}, inserted: {}"),
    
    /**
     * 交易總額計算發生異常
     * 參數: settlementDate
     */
    E_SERVICE_TRANSACTION_TOTAL_ERROR("Service: Exception occurred during transaction total calculation, settlement date: {}"),
    
    /**
     * 開始 UD AR 對帳
     * 參數: settlementDate
     */
    I_SERVICE_UD_AR_RECONCILIATION_START("Service: Starting UD AR reconciliation, settlement date: {}"),
    
    /**
     * 未找到 UD AR 對帳數據
     * 參數: settlementDate
     */
    SERVICE_UD_AR_RECONCILIATION_NOT_FOUND("Service: No UD AR reconciliation data found for settlement date: {}"),
    
    /**
     * UD AR 對帳成功
     * 參數: settlementDate, insertedRows
     */
    I_SERVICE_UD_AR_RECONCILIATION_SUCCESS("Service: UD AR reconciliation succeeded, settlement date: {}, inserted rows: {}"),
    
    /**
     * UD AR 對帳部分失敗
     * 參數: settlementDate, expected, inserted
     */
    SERVICE_UD_AR_RECONCILIATION_PARTIAL_FAIL("Service: UD AR reconciliation partially failed, settlement date: {}, expected: {}, inserted: {}"),
    
    /**
     * UD AR 對帳發生異常
     * 參數: settlementDate
     */
    E_SERVICE_UD_AR_RECONCILIATION_ERROR("Service: Exception occurred during UD AR reconciliation, settlement date: {}"),
    
    /**
     * 處理交易時發生錯誤
     * 參數: deviceId, seqNum
     */
    E_SERVICE_AUDIT_REGISTER_TRANSACTION_ERROR("Error processing transaction for deviceId: {}, seqNum: {}"),
    
    /**
     * 處理審計註冊請求時發生意外錯誤
     */
    E_SERVICE_AUDIT_REGISTER_UNEXPECTED_ERROR("Unexpected error processing audit register request"),
    
    /**
     * 檢測到設備重啟
     * 參數: deviceId, beId, businessDate, seqNum
     */
    SERVICE_DEVICE_RESTART_DETECTED("Device restart detected for deviceId: {}, beId: {}, businessDate: {}. " +
            "Current AR seqNum: {}, Previous max seqNum exists. " +
            "Will use accumulated counts from database."),
    
    /**
     * 處理前一個業務日期的未完成交易
     * 參數: deviceId, businessDate, transactionDateTime, currentDate
     */
    I_SERVICE_PROCESSING_OUTSTANDING_TRANSACTIONS("Processing outstanding transactions from previous business date. " +
            "DeviceId: {}, BusinessDate: {}, TransactionDateTime: {}, CurrentDate: {}. " +
            "This may be outstanding transactions that were not uploaded before device shutdown."),
    
    /**
     * 業務日期在未來
     * 參數: deviceId, businessDate, currentDate
     */
    SERVICE_BUSINESS_DATE_IN_FUTURE("Business date is in the future. DeviceId: {}, BusinessDate: {}, CurrentDate: {}"),
    
    /**
     * 交易日期和業務日期不匹配
     * 參數: deviceId, businessDate, transactionDate
     */
    SERVICE_DATE_MISMATCH("Transaction date and business date mismatch. " +
            "DeviceId: {}, BusinessDate: {}, TransactionDate: {}. " +
            "This may indicate data inconsistency."),
    
    /**
     * 更新跨日期摘要
     * 參數: deviceId, businessDate, currentDate, arType, cardMediaType, previousCount, previousValue, currentCount, currentValue, newTotalCount, newTotalValue
     */
    I_SERVICE_UPDATE_CROSS_DATE_SUMMARY("Updating cross-date summary for deviceId: {}, businessDate: {}, currentDate: {}. " +
            "arType: {}, cardMediaType: {}. " +
            "Previous total: count={}, value={}. Current: count={}, value={}. " +
            "New total: count={}, value={}"),
    
    /**
     * 更新摘要（調試級別）
     * 參數: deviceId, arType, cardMediaType, previousCount, previousValue, currentCount, currentValue, newTotalCount, newTotalValue
     */
    SERVICE_UPDATE_SUMMARY_DEBUG("Updated summary for deviceId: {}, arType: {}, cardMediaType: {}. " +
            "Previous total: count={}, value={}. Current: count={}, value={}. " +
            "New total: count={}, value={}"),
    
    /**
     * 創建跨日期摘要
     * 參數: deviceId, businessDate, currentDate, arType, cardMediaType, initialCount, initialValue
     */
    I_SERVICE_CREATE_CROSS_DATE_SUMMARY("Creating cross-date summary for deviceId: {}, businessDate: {}, currentDate: {}. " +
            "arType: {}, cardMediaType: {}. " +
            "Initial: count={}, value={}"),
    
    /**
     * 創建新摘要（調試級別）
     * 參數: deviceId, arType, cardMediaType, initialCount, initialValue
     */
    SERVICE_CREATE_SUMMARY_DEBUG("Created new summary for deviceId: {}, arType: {}, cardMediaType: {}. " +
            "Initial: count={}, value={}"),
    
    /**
     * 保存到異常表失敗
     */
    E_SERVICE_SAVE_TO_EXCEPTION_TABLES_ERROR("Failed to save to exception tables"),

    // ==================== Batch 日誌訊息 ====================
    
    /**
     * 開始執行 TransactionTotalSummaryTasklet
     */
    I_BATCH_TRANSACTION_TOTAL_TASKLET_START("Starting TransactionTotalSummaryTasklet execution"),
    
    /**
     * 開始執行 UdArReconciliationTasklet
     */
    I_BATCH_UD_AR_TASKLET_START("Starting UdArReconciliationTasklet execution"),
    
    /**
     * 從 JobParameters 檢索結算日期
     * 參數: settlementDate
     */
    I_BATCH_RETRIEVE_SETTLEMENT_DATE("Retrieved settlement date from JobParameters: {}"),
    
    /**
     * 開始向後處理
     * 參數: startDate, settlementDate
     */
    I_BATCH_START_BACKWARD_PROCESSING("Starting backward processing, date range: {} to {} (7 days total)"),
    
    /**
     * 處理日期
     * 參數: dayNumber, currentDate
     */
    I_BATCH_PROCESSING_DATE("Processing date {}/7: {}"),
    
    /**
     * 日期處理成功
     * 參數: currentDate
     */
    I_BATCH_DATE_PROCESSED_SUCCESS("Date {} processed successfully"),
    
    /**
     * 日期處理失敗
     * 參數: currentDate
     */
    E_BATCH_DATE_PROCESSED_FAIL("Date {} processing failed"),
    
    /**
     * 處理日期時發生異常
     * 參數: currentDate
     */
    E_BATCH_DATE_PROCESSING_EXCEPTION("Exception occurred while processing date {}"),
    
    /**
     * 所有日期處理失敗
     * 參數: successCount, failureCount
     */
    E_BATCH_ALL_DATES_FAILED("All dates processing failed, success: {}, failure: {}"),
    
    /**
     * 部分日期處理失敗
     * 參數: successCount, failureCount
     */
    BATCH_PARTIAL_DATES_FAILED("Partial dates processing failed, success: {}, failure: {}"),
    
    /**
     * TransactionTotalSummaryTasklet 執行完成
     * 參數: successCount, failureCount
     */
    I_BATCH_TRANSACTION_TOTAL_TASKLET_COMPLETED("TransactionTotalSummaryTasklet execution completed, success: {}, failure: {}"),
    
    /**
     * UdArReconciliationTasklet 執行完成
     * 參數: successCount, failureCount
     */
    I_BATCH_UD_AR_TASKLET_COMPLETED("UdArReconciliationTasklet execution completed, success: {}, failure: {}"),
    
    /**
     * TransactionTotalSummaryTasklet 執行時發生異常
     */
    E_BATCH_TRANSACTION_TOTAL_TASKLET_EXCEPTION("Exception occurred during TransactionTotalSummaryTasklet execution"),
    
    /**
     * UdArReconciliationTasklet 執行時發生異常
     */
    E_BATCH_UD_AR_TASKLET_EXCEPTION("Exception occurred during UdArReconciliationTasklet execution"),

    // ==================== Exception Handler 日誌訊息 ====================
    
    /**
     * 驗證錯誤
     * 參數: errors
     */
    EXCEPTION_VALIDATION_ERRORS("Validation errors: {}"),
    
    /**
     * 發生意外錯誤
     */
    E_EXCEPTION_UNEXPECTED_ERROR("Unexpected error occurred");

    private final String messageTemplate;

    LogMessage(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    /**
     * 獲取日誌訊息模板
     * 
     * @return 訊息模板字串
     */
    public String getMessage() {
        return messageTemplate;
    }

    /**
     * 格式化日誌訊息（使用 SLF4J 的參數化日誌格式）
     * 
     * @param args 參數陣列
     * @return 格式化後的訊息（僅供參考，實際使用時直接傳遞給 log.info/warn/error）
     */
    public String format(Object... args) {
        // 注意：SLF4J 會自動處理參數化日誌，此方法僅供參考
        // 實際使用時應該直接使用：log.info(LogMessage.XXX.getMessage(), args)
        return messageTemplate;
    }
}

