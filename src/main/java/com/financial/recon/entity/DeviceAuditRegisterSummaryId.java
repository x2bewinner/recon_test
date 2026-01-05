package com.financial.recon.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceAuditRegisterSummaryId implements Serializable {
    private String deviceId;
    private Integer beId;
    private LocalDate businessDate;
    private String arTypeIdentifier;
    private String cardMediaTypeId;
}

