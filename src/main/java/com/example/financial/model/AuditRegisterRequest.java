package com.example.financial.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AuditRegisterRequest {
    private String deviceId;
    private Long totalTransactions;
    private BigDecimal totalAmount;
    private LocalDate auditDate;
}
