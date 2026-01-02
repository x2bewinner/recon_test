package com.example.financial.controller;

import com.example.financial.model.AuditRecord;
import com.example.financial.model.AuditRegisterRequest;
import com.example.financial.repository.AuditRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    @Autowired
    private AuditRecordRepository auditRecordRepository;

    @PostMapping("/register")
    public ResponseEntity<String> registerAudit(@RequestBody AuditRegisterRequest request) {
        AuditRecord record = new AuditRecord();
        record.setDeviceId(request.getDeviceId());
        record.setTotalTransactions(request.getTotalTransactions());
        record.setTotalAmount(request.getTotalAmount());
        record.setAuditDate(request.getAuditDate());
        record.setReconciliationStatus("PENDING");

        auditRecordRepository.save(record);

        return ResponseEntity.ok("Audit request received and queued for reconciliation.");
    }
}
