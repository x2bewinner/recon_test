package com.financial.recon.batch;

import com.financial.recon.service.TransactionTotalSummaryService;
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
        log.info("Starting TransactionTotalSummaryTasklet execution");
        
        try {
            // Get settlementDate from JobParameters
            String settlementDateStr = chunkContext.getStepContext()
                    .getJobParameters()
                    .get("settlementDate")
                    .toString();
            
            LocalDate settlementDate = LocalDate.parse(settlementDateStr, DATE_FORMATTER);
            log.info("Retrieved settlement date from JobParameters: {}", settlementDate);
            
            // Process 7 days backwards (inclusive of current day)
            // From settlementDate - 6 days to settlementDate, total 7 days
            LocalDate startDate = settlementDate.minusDays(6);
            log.info("Starting backward processing, date range: {} to {} (7 days total)", startDate, settlementDate);
            
            int successCount = 0;
            int failureCount = 0;
            
            // Loop through each day
            for (int i = 0; i < 7; i++) {
                LocalDate currentDate = startDate.plusDays(i);
                log.info("Processing date {}/7: {}", i + 1, currentDate);
                
                try {
                    boolean success = transactionTotalSummaryService.calculateTransactionTotal(currentDate);
                    
                    if (success) {
                        successCount++;
                        log.info("Date {} processed successfully", currentDate);
                    } else {
                        failureCount++;
                        log.error("Date {} processing failed", currentDate);
                        // Continue processing other dates, don't throw exception immediately
                    }
                } catch (Exception e) {
                    failureCount++;
                    log.error("Exception occurred while processing date {}", currentDate, e);
                    // Continue processing other dates
                }
            }
            
            // If all dates failed, throw exception
            if (successCount == 0) {
                log.error("All dates processing failed, success: {}, failure: {}", successCount, failureCount);
                throw new RuntimeException("Failed to calculate transaction total: all dates processing failed");
            }
            
            // If partial failure, log warning but continue
            if (failureCount > 0) {
                log.warn("Partial dates processing failed, success: {}, failure: {}", successCount, failureCount);
            }
            
            log.info("TransactionTotalSummaryTasklet execution completed, success: {}, failure: {}", successCount, failureCount);
            contribution.incrementWriteCount(successCount);
            
            return RepeatStatus.FINISHED;
            
        } catch (Exception e) {
            log.error("Exception occurred during TransactionTotalSummaryTasklet execution", e);
            throw e;
        }
    }
}

