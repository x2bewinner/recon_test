package com.xxcards.xbtx.udar.constant;

/**
 * Log message enumeration class
 * Centralized management of all log message templates in the application
 */
public enum LogMessage {

    // ==================== Controller Log Messages ====================
    
    /**
     * Audit register request received
     * Parameters: clientRequestId, transactionCount
     */
    I_CONTROLLER_AUDIT_REGISTER_RECEIVED("Received audit register request. ClientRequestId: {}, TransactionCount: {}"),
    
    /**
     * Settlement date not provided, using current date
     * Parameters: settlementDate
     */
    I_CONTROLLER_SETTLEMENT_DATE_NOT_PROVIDED("Settlement date not provided, using current date: {}"),
    
    /**
     * Starting to trigger transaction total calculation batch job
     * Parameters: settlementDate
     */
    I_CONTROLLER_TRIGGER_TRANSACTION_TOTAL_CALCULATION("Starting to trigger transaction total calculation batch job, settlement date: {}"),
    
    /**
     * Starting to trigger UD AR reconciliation batch job
     * Parameters: settlementDate
     */
    I_CONTROLLER_TRIGGER_UD_AR_RECONCILIATION("Starting to trigger UD AR reconciliation batch job, settlement date: {}"),
    
    /**
     * Batch job started successfully
     * Parameters: settlementDate
     */
    I_CONTROLLER_BATCH_JOB_STARTED_SUCCESS("Batch job started successfully, settlement date: {}"),
    
    /**
     * UD AR reconciliation batch job started successfully
     * Parameters: settlementDate
     */
    I_CONTROLLER_UD_AR_BATCH_JOB_STARTED_SUCCESS("UD AR reconciliation batch job started successfully, settlement date: {}"),
    
    /**
     * Exception occurred while starting batch job
     * Parameters: settlementDate
     */
    E_CONTROLLER_BATCH_JOB_START_ERROR("Exception occurred while starting batch job, settlement date: {}"),
    
    /**
     * Exception occurred while starting UD AR reconciliation batch job
     * Parameters: settlementDate
     */
    E_CONTROLLER_UD_AR_BATCH_JOB_START_ERROR("Exception occurred while starting UD AR reconciliation batch job, settlement date: {}"),

    // ==================== Service Log Messages ====================
    
    /**
     * Starting transaction total calculation
     * Parameters: settlementDate
     */
    I_SERVICE_TRANSACTION_TOTAL_START("Service: Starting transaction total calculation, settlement date: {}"),
    
    /**
     * No transaction totals data found
     * Parameters: settlementDate
     */
    SERVICE_TRANSACTION_TOTAL_NOT_FOUND("Service: No transaction totals found for settlement date: {}"),
    
    /**
     * Transaction total calculation succeeded
     * Parameters: settlementDate, insertedRows
     */
    I_SERVICE_TRANSACTION_TOTAL_SUCCESS("Service: Transaction total calculation succeeded, settlement date: {}, inserted rows: {}"),
    
    /**
     * Transaction total calculation partially failed
     * Parameters: settlementDate, expected, inserted
     */
    SERVICE_TRANSACTION_TOTAL_PARTIAL_FAIL("Service: Transaction total calculation partially failed, settlement date: {}, expected: {}, inserted: {}"),
    
    /**
     * Exception occurred during transaction total calculation
     * Parameters: settlementDate
     */
    E_SERVICE_TRANSACTION_TOTAL_ERROR("Service: Exception occurred during transaction total calculation, settlement date: {}"),
    
    /**
     * Starting UD AR reconciliation
     * Parameters: settlementDate
     */
    I_SERVICE_UD_AR_RECONCILIATION_START("Service: Starting UD AR reconciliation, settlement date: {}"),
    
    /**
     * No UD AR reconciliation data found
     * Parameters: settlementDate
     */
    SERVICE_UD_AR_RECONCILIATION_NOT_FOUND("Service: No UD AR reconciliation data found for settlement date: {}"),
    
    /**
     * UD AR reconciliation succeeded
     * Parameters: settlementDate, insertedRows
     */
    I_SERVICE_UD_AR_RECONCILIATION_SUCCESS("Service: UD AR reconciliation succeeded, settlement date: {}, inserted rows: {}"),
    
    /**
     * UD AR reconciliation partially failed
     * Parameters: settlementDate, expected, inserted
     */
    SERVICE_UD_AR_RECONCILIATION_PARTIAL_FAIL("Service: UD AR reconciliation partially failed, settlement date: {}, expected: {}, inserted: {}"),
    
    /**
     * Exception occurred during UD AR reconciliation
     * Parameters: settlementDate
     */
    E_SERVICE_UD_AR_RECONCILIATION_ERROR("Service: Exception occurred during UD AR reconciliation, settlement date: {}"),
    
    /**
     * Error processing transaction
     * Parameters: deviceId, seqNum
     */
    E_SERVICE_AUDIT_REGISTER_TRANSACTION_ERROR("Error processing transaction for deviceId: {}, seqNum: {}"),
    
    /**
     * Unexpected error processing audit register request
     */
    E_SERVICE_AUDIT_REGISTER_UNEXPECTED_ERROR("Unexpected error processing audit register request"),
    
    /**
     * Device restart detected
     * Parameters: deviceId, beId, businessDate, seqNum
     */
    SERVICE_DEVICE_RESTART_DETECTED("Device restart detected for deviceId: {}, beId: {}, businessDate: {}. " +
            "Current AR seqNum: {}, Previous max seqNum exists. " +
            "Will use accumulated counts from database."),
    
    /**
     * Processing outstanding transactions from previous business date
     * Parameters: deviceId, businessDate, transactionDateTime, currentDate
     */
    I_SERVICE_PROCESSING_OUTSTANDING_TRANSACTIONS("Processing outstanding transactions from previous business date. " +
            "DeviceId: {}, BusinessDate: {}, TransactionDateTime: {}, CurrentDate: {}. " +
            "This may be outstanding transactions that were not uploaded before device shutdown."),
    
    /**
     * Business date is in the future
     * Parameters: deviceId, businessDate, currentDate
     */
    SERVICE_BUSINESS_DATE_IN_FUTURE("Business date is in the future. DeviceId: {}, BusinessDate: {}, CurrentDate: {}"),
    
    /**
     * Transaction date and business date mismatch
     * Parameters: deviceId, businessDate, transactionDate
     */
    SERVICE_DATE_MISMATCH("Transaction date and business date mismatch. " +
            "DeviceId: {}, BusinessDate: {}, TransactionDate: {}. " +
            "This may indicate data inconsistency."),
    
    /**
     * Updating cross-date summary
     * Parameters: deviceId, businessDate, currentDate, arType, cardMediaType, previousCount, previousValue, currentCount, currentValue, newTotalCount, newTotalValue
     */
    I_SERVICE_UPDATE_CROSS_DATE_SUMMARY("Updating cross-date summary for deviceId: {}, businessDate: {}, currentDate: {}. " +
            "arType: {}, cardMediaType: {}. " +
            "Previous total: count={}, value={}. Current: count={}, value={}. " +
            "New total: count={}, value={}"),
    
    /**
     * Updated summary (debug level)
     * Parameters: deviceId, arType, cardMediaType, previousCount, previousValue, currentCount, currentValue, newTotalCount, newTotalValue
     */
    SERVICE_UPDATE_SUMMARY_DEBUG("Updated summary for deviceId: {}, arType: {}, cardMediaType: {}. " +
            "Previous total: count={}, value={}. Current: count={}, value={}. " +
            "New total: count={}, value={}"),
    
    /**
     * Creating cross-date summary
     * Parameters: deviceId, businessDate, currentDate, arType, cardMediaType, initialCount, initialValue
     */
    I_SERVICE_CREATE_CROSS_DATE_SUMMARY("Creating cross-date summary for deviceId: {}, businessDate: {}, currentDate: {}. " +
            "arType: {}, cardMediaType: {}. " +
            "Initial: count={}, value={}"),
    
    /**
     * Created new summary (debug level)
     * Parameters: deviceId, arType, cardMediaType, initialCount, initialValue
     */
    SERVICE_CREATE_SUMMARY_DEBUG("Created new summary for deviceId: {}, arType: {}, cardMediaType: {}. " +
            "Initial: count={}, value={}"),
    
    /**
     * Failed to save to exception tables
     */
    E_SERVICE_SAVE_TO_EXCEPTION_TABLES_ERROR("Failed to save to exception tables"),

    // ==================== Batch Log Messages ====================
    
    /**
     * Starting TransactionTotalSummaryTasklet execution
     */
    I_BATCH_TRANSACTION_TOTAL_TASKLET_START("Starting TransactionTotalSummaryTasklet execution"),
    
    /**
     * Starting UdArReconciliationTasklet execution
     */
    I_BATCH_UD_AR_TASKLET_START("Starting UdArReconciliationTasklet execution"),
    
    /**
     * Retrieved settlement date from JobParameters
     * Parameters: settlementDate
     */
    I_BATCH_RETRIEVE_SETTLEMENT_DATE("Retrieved settlement date from JobParameters: {}"),
    
    /**
     * Starting backward processing
     * Parameters: startDate, settlementDate
     */
    I_BATCH_START_BACKWARD_PROCESSING("Starting backward processing, date range: {} to {} (7 days total)"),
    
    /**
     * Processing date
     * Parameters: dayNumber, currentDate
     */
    I_BATCH_PROCESSING_DATE("Processing date {}/7: {}"),
    
    /**
     * Date processed successfully
     * Parameters: currentDate
     */
    I_BATCH_DATE_PROCESSED_SUCCESS("Date {} processed successfully"),
    
    /**
     * Date processing failed
     * Parameters: currentDate
     */
    E_BATCH_DATE_PROCESSED_FAIL("Date {} processing failed"),
    
    /**
     * Exception occurred while processing date
     * Parameters: currentDate
     */
    E_BATCH_DATE_PROCESSING_EXCEPTION("Exception occurred while processing date {}"),
    
    /**
     * All dates processing failed
     * Parameters: successCount, failureCount
     */
    E_BATCH_ALL_DATES_FAILED("All dates processing failed, success: {}, failure: {}"),
    
    /**
     * Partial dates processing failed
     * Parameters: successCount, failureCount
     */
    BATCH_PARTIAL_DATES_FAILED("Partial dates processing failed, success: {}, failure: {}"),
    
    /**
     * TransactionTotalSummaryTasklet execution completed
     * Parameters: successCount, failureCount
     */
    I_BATCH_TRANSACTION_TOTAL_TASKLET_COMPLETED("TransactionTotalSummaryTasklet execution completed, success: {}, failure: {}"),
    
    /**
     * UdArReconciliationTasklet execution completed
     * Parameters: successCount, failureCount
     */
    I_BATCH_UD_AR_TASKLET_COMPLETED("UdArReconciliationTasklet execution completed, success: {}, failure: {}"),
    
    /**
     * Exception occurred during TransactionTotalSummaryTasklet execution
     */
    E_BATCH_TRANSACTION_TOTAL_TASKLET_EXCEPTION("Exception occurred during TransactionTotalSummaryTasklet execution"),
    
    /**
     * Exception occurred during UdArReconciliationTasklet execution
     */
    E_BATCH_UD_AR_TASKLET_EXCEPTION("Exception occurred during UdArReconciliationTasklet execution"),

    // ==================== Exception Handler Log Messages ====================
    
    /**
     * Validation errors
     * Parameters: errors
     */
    EXCEPTION_VALIDATION_ERRORS("Validation errors: {}"),
    
    /**
     * Unexpected error occurred
     */
    E_EXCEPTION_UNEXPECTED_ERROR("Unexpected error occurred");

    private final String messageTemplate;

    LogMessage(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    /**
     * Get log message template
     * 
     * @return Message template string
     */
    public String getMessage() {
        return messageTemplate;
    }

    /**
     * Format log message (using SLF4J parameterized log format)
     * 
     * @param args Parameter array
     * @return Formatted message (for reference only, in actual use directly pass to log.info/warn/error)
     */
    public String format(Object... args) {
        // Note: SLF4J automatically handles parameterized logging, this method is for reference only
        // In actual use, directly use: log.info(LogMessage.XXX.getMessage(), args)
        return messageTemplate;
    }
}

