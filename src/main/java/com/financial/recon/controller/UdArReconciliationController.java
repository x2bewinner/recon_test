package com.financial.recon.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1/batch")
@RequiredArgsConstructor
public class UdArReconciliationController {

    private final JobLauncher jobLauncher;
    private final Job udArReconciliationJob;

    /**
     * Trigger batch job for UD AR reconciliation
     * 
     * @param settlementDate Settlement date (format: yyyy-MM-dd), uses current date if not provided
     * @return Execution result
     */
    @PostMapping("/udArReconciliation")
    public ResponseEntity<Map<String, Object>> triggerUdArReconciliation(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate settlementDate) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // If settlement date not provided, use current date
            if (settlementDate == null) {
                settlementDate = LocalDate.now();
                log.info("Settlement date not provided, using current date: {}", settlementDate);
            }
            
            log.info("Starting to trigger UD AR reconciliation batch job, settlement date: {}", settlementDate);
            
            // Create Job parameters, use timestamp to ensure each execution is a new Job Instance
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .addString("settlementDate", settlementDate.toString())
                    .toJobParameters();
            
            // Launch Job
            jobLauncher.run(udArReconciliationJob, jobParameters);
            
            response.put("status", "SUCCESS");
            response.put("message", "UD AR reconciliation batch job started successfully");
            response.put("settlementDate", settlementDate.toString());
            log.info("UD AR reconciliation batch job started successfully, settlement date: {}", settlementDate);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Exception occurred while starting UD AR reconciliation batch job, settlement date: {}", settlementDate, e);
            response.put("status", "ERROR");
            response.put("message", "Failed to start UD AR reconciliation batch job: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}

