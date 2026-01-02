package com.example.financial.task;

import com.example.financial.service.ReconciliationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledReconciliationTask {

    @Autowired
    private ReconciliationService reconciliationService;

    // Run every minute for demonstration purposes
    @Scheduled(fixedRate = 60000)
    public void runReconciliationTask() {
        reconciliationService.performReconciliation();
    }
}
