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
public class UdArReconciliationBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final UdArReconciliationTasklet udArReconciliationTasklet;

    /**
     * Define Step for UD AR reconciliation
     */
    @Bean
    public Step udArReconciliationStep() {
        return new StepBuilder("udArReconciliationStep", jobRepository)
                .tasklet(udArReconciliationTasklet, transactionManager)
                .build();
    }

    /**
     * Define Job for UD AR reconciliation
     */
    @Bean
    public Job udArReconciliationJob() {
        return new JobBuilder("udArReconciliationJob", jobRepository)
                .start(udArReconciliationStep())
                .build();
    }
}

