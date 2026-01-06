package com.financial.recon.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TransactionTotalSummaryBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TransactionTotalSummaryTasklet transactionTotalSummaryTasklet;

    /**
     * Define Step for calculating transaction total
     */
    @Bean
    public Step calculateTransactionTotalStep() {
        return new StepBuilder("calculateTransactionTotalStep", jobRepository)
                .tasklet(transactionTotalSummaryTasklet, transactionManager)
                .build();
    }

    /**
     * Define Job for calculating transaction total
     */
    @Bean
    public Job calculateTransactionTotalJob() {
        return new JobBuilder("calculateTransactionTotalJob", jobRepository)
                .start(calculateTransactionTotalStep())
                .build();
    }
}

