package com.xxcards.xbtx.udar.batch;

import com.xxcards.xbtx.udar.constant.LogMessage;
import com.xxcards.xbtx.udar.service.TransactionTotalSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionTotalSummaryTasklet implements Tasklet {

    private final TransactionTotalSummaryService transactionTotalSummaryService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info(LogMessage.I_BATCH_TRANSACTION_TOTAL_TASKLET_START.getMessage());
        
        try {
            // Get settlementDate from JobParameters
            String settlementDateStr = chunkContext.getStepContext()
                    .getJobParameters()
                    .get("settlementDate")
                    .toString();
            
            LocalDate settlementDate = LocalDate.parse(settlementDateStr, DATE_FORMATTER);
            log.info(LogMessage.I_BATCH_RETRIEVE_SETTLEMENT_DATE.getMessage(), settlementDate);
            
            // Process 7 days backwards (inclusive of current day)
            // From settlementDate - 6 days to settlementDate, total 7 days
            LocalDate startDate = settlementDate.minusDays(6);
            log.info(LogMessage.I_BATCH_START_BACKWARD_PROCESSING.getMessage(), startDate, settlementDate);
            
            int successCount = 0;
            int failureCount = 0;
            
            // Loop through each day
            for (int i = 0; i < 7; i++) {
                LocalDate currentDate = startDate.plusDays(i);
                log.info(LogMessage.I_BATCH_PROCESSING_DATE.getMessage(), i + 1, currentDate);
                
                try {
                    boolean success = transactionTotalSummaryService.calculateTransactionTotal(currentDate);
                    
                    if (success) {
                        successCount++;
                        log.info(LogMessage.I_BATCH_DATE_PROCESSED_SUCCESS.getMessage(), currentDate);
                    } else {
                        failureCount++;
                        log.error(LogMessage.E_BATCH_DATE_PROCESSED_FAIL.getMessage(), currentDate);
                        // Continue processing other dates, don't throw exception immediately
                    }
                } catch (Exception e) {
                    failureCount++;
                    log.error(LogMessage.E_BATCH_DATE_PROCESSING_EXCEPTION.getMessage(), currentDate, e);
                    // Continue processing other dates
                }
            }
            
            // If all dates failed, throw exception
            if (successCount == 0) {
                log.error(LogMessage.E_BATCH_ALL_DATES_FAILED.getMessage(), successCount, failureCount);
                throw new RuntimeException("Failed to calculate transaction total: all dates processing failed");
            }
            
            // If partial failure, log warning but continue
            if (failureCount > 0) {
                log.warn(LogMessage.BATCH_PARTIAL_DATES_FAILED.getMessage(), successCount, failureCount);
            }
            
            log.info(LogMessage.I_BATCH_TRANSACTION_TOTAL_TASKLET_COMPLETED.getMessage(), successCount, failureCount);
            contribution.incrementWriteCount(successCount);
            
            return RepeatStatus.FINISHED;
            
        } catch (Exception e) {
            log.error(LogMessage.E_BATCH_TRANSACTION_TOTAL_TASKLET_EXCEPTION.getMessage(), e);
            throw e;
        }
    }
}

