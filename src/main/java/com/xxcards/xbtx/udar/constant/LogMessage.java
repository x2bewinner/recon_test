package com.xxcards.xbtx.udar.constant;

/**
 * Log message enumeration class
 * Centralized management of all log message templates in the application
 */
public enum LogMessage {

    // ==================== Controller Log Messages ====================
    
    I_CONTROLLER_AUDIT_REGISTER_RECEIVED(1001, "Received audit register request. ClientRequestId: {}, TransactionCount: {}"),
    I_CONTROLLER_SETTLEMENT_DATE_NOT_PROVIDED(1002, "Settlement date not provided, using current date: {}"),
    I_CONTROLLER_TRIGGER_TRANSACTION_TOTAL_CALCULATION(1003, "Starting to trigger transaction total calculation batch job, settlement date: {}"),
    I_CONTROLLER_TRIGGER_UD_AR_RECONCILIATION(1004, "Starting to trigger UD AR reconciliation batch job, settlement date: {}"),
    I_CONTROLLER_BATCH_JOB_STARTED_SUCCESS(1005, "Batch job started successfully, settlement date: {}"),
    I_CONTROLLER_UD_AR_BATCH_JOB_STARTED_SUCCESS(1006, "UD AR reconciliation batch job started successfully, settlement date: {}"),
    E_CONTROLLER_BATCH_JOB_START_ERROR(1901, "Exception occurred while starting batch job, settlement date: {}"),
    E_CONTROLLER_UD_AR_BATCH_JOB_START_ERROR(1902, "Exception occurred while starting UD AR reconciliation batch job, settlement date: {}"),

    // ==================== Service Log Messages ====================
    
    I_SERVICE_TRANSACTION_TOTAL_START(2001, "Service: Starting transaction total calculation, settlement date: {}"),
    SERVICE_TRANSACTION_TOTAL_NOT_FOUND(2002, "Service: No transaction totals found for settlement date: {}"),
    I_SERVICE_TRANSACTION_TOTAL_SUCCESS(2003, "Service: Transaction total calculation succeeded, settlement date: {}, inserted rows: {}"),
    SERVICE_TRANSACTION_TOTAL_PARTIAL_FAIL(2004, "Service: Transaction total calculation partially failed, settlement date: {}, expected: {}, inserted: {}"),
    E_SERVICE_TRANSACTION_TOTAL_ERROR(2901, "Service: Exception occurred during transaction total calculation, settlement date: {}"),
    I_SERVICE_UD_AR_RECONCILIATION_START(2005, "Service: Starting UD AR reconciliation, settlement date: {}"),
    SERVICE_UD_AR_RECONCILIATION_NOT_FOUND(2006, "Service: No UD AR reconciliation data found for settlement date: {}"),
    I_SERVICE_UD_AR_RECONCILIATION_SUCCESS(2007, "Service: UD AR reconciliation succeeded, settlement date: {}, inserted rows: {}"),
    SERVICE_UD_AR_RECONCILIATION_PARTIAL_FAIL(2008, "Service: UD AR reconciliation partially failed, settlement date: {}, expected: {}, inserted: {}"),
    E_SERVICE_UD_AR_RECONCILIATION_ERROR(2902, "Service: Exception occurred during UD AR reconciliation, settlement date: {}"),
    E_SERVICE_AUDIT_REGISTER_TRANSACTION_ERROR(2903, "Error processing transaction for deviceId: {}, seqNum: {}"),
    E_SERVICE_AUDIT_REGISTER_UNEXPECTED_ERROR(2904, "Unexpected error processing audit register request"),
    SERVICE_DEVICE_RESTART_DETECTED(2009, "Device restart detected for deviceId: {}, beId: {}, businessDate: {}. " +
            "Current AR seqNum: {}, Previous max seqNum exists. " +
            "Will use accumulated counts from database."),
    I_SERVICE_PROCESSING_OUTSTANDING_TRANSACTIONS(2010, "Processing outstanding transactions from previous business date. " +
            "DeviceId: {}, BusinessDate: {}, TransactionDateTime: {}, CurrentDate: {}. " +
            "This may be outstanding transactions that were not uploaded before device shutdown."),
    SERVICE_BUSINESS_DATE_IN_FUTURE(2011, "Business date is in the future. DeviceId: {}, BusinessDate: {}, CurrentDate: {}"),
    SERVICE_DATE_MISMATCH(2012, "Transaction date and business date mismatch. " +
            "DeviceId: {}, BusinessDate: {}, TransactionDate: {}. " +
            "This may indicate data inconsistency."),
    I_SERVICE_UPDATE_CROSS_DATE_SUMMARY(2013, "Updating cross-date summary for deviceId: {}, businessDate: {}, currentDate: {}. " +
            "arType: {}, cardMediaType: {}. " +
            "Previous total: count={}, value={}. Current: count={}, value={}. " +
            "New total: count={}, value={}"),
    SERVICE_UPDATE_SUMMARY_DEBUG(2014, "Updated summary for deviceId: {}, arType: {}, cardMediaType: {}. " +
            "Previous total: count={}, value={}. Current: count={}, value={}. " +
            "New total: count={}, value={}"),
    I_SERVICE_CREATE_CROSS_DATE_SUMMARY(2015, "Creating cross-date summary for deviceId: {}, businessDate: {}, currentDate: {}. " +
            "arType: {}, cardMediaType: {}. " +
            "Initial: count={}, value={}"),
    SERVICE_CREATE_SUMMARY_DEBUG(2016, "Created new summary for deviceId: {}, arType: {}, cardMediaType: {}. " +
            "Initial: count={}, value={}"),
    E_SERVICE_SAVE_TO_EXCEPTION_TABLES_ERROR(2905, "Failed to save to exception tables"),

    // ==================== Batch Log Messages ====================
    
    I_BATCH_TRANSACTION_TOTAL_TASKLET_START(3001, "Starting TransactionTotalSummaryTasklet execution"),
    I_BATCH_UD_AR_TASKLET_START(3002, "Starting UdArReconciliationTasklet execution"),
    I_BATCH_RETRIEVE_SETTLEMENT_DATE(3003, "Retrieved settlement date from JobParameters: {}"),
    I_BATCH_START_BACKWARD_PROCESSING(3004, "Starting backward processing, date range: {} to {} (7 days total)"),
    I_BATCH_PROCESSING_DATE(3005, "Processing date {}/7: {}"),
    I_BATCH_DATE_PROCESSED_SUCCESS(3006, "Date {} processed successfully"),
    E_BATCH_DATE_PROCESSED_FAIL(3901, "Date {} processing failed"),
    E_BATCH_DATE_PROCESSING_EXCEPTION(3902, "Exception occurred while processing date {}"),
    E_BATCH_ALL_DATES_FAILED(3903, "All dates processing failed, success: {}, failure: {}"),
    BATCH_PARTIAL_DATES_FAILED(3007, "Partial dates processing failed, success: {}, failure: {}"),
    I_BATCH_TRANSACTION_TOTAL_TASKLET_COMPLETED(3008, "TransactionTotalSummaryTasklet execution completed, success: {}, failure: {}"),
    I_BATCH_UD_AR_TASKLET_COMPLETED(3009, "UdArReconciliationTasklet execution completed, success: {}, failure: {}"),
    E_BATCH_TRANSACTION_TOTAL_TASKLET_EXCEPTION(3904, "Exception occurred during TransactionTotalSummaryTasklet execution"),
    E_BATCH_UD_AR_TASKLET_EXCEPTION(3905, "Exception occurred during UdArReconciliationTasklet execution"),

    // ==================== Exception Handler Log Messages ====================
    
    EXCEPTION_VALIDATION_ERRORS(4001, "Validation errors: {}"),
    E_EXCEPTION_UNEXPECTED_ERROR(4901, "Unexpected error occurred");

    private final int code;
    private final String description;

    LogMessage(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Get log message code
     * 
     * @return Message code
     */
    public int getCode() {
        return code;
    }

    /**
     * Get log message description
     * 
     * @return Message description string
     */
    public String getMessage() {
        return description;
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
        return description;
    }
}
