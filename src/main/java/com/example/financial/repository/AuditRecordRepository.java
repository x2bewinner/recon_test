package com.example.financial.repository;

import com.example.financial.model.AuditRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AuditRecordRepository extends JpaRepository<AuditRecord, Long> {
    List<AuditRecord> findByReconciliationStatus(String status);
    List<AuditRecord> findByDeviceIdAndAuditDate(String deviceId, LocalDate auditDate);
}
