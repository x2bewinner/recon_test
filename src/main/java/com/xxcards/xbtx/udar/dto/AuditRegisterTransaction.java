package com.xxcards.xbtx.udar.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class AuditRegisterTransaction {
    @NotBlank(message = "transactionType is required")
    private String transactionType;

    @NotNull(message = "transactionDateTime is required")
    private OffsetDateTime transactionDateTime;

    private String deviceTypeId;

    @NotBlank(message = "equipmentId is required")
    private String equipmentId;

    private String deviceSpecialMode;

    @NotBlank(message = "deviceId is required")
    private String deviceId;

    private String serviceId;

    @NotNull(message = "beId is required")
    private Integer beId;

    @NotNull(message = "auditRegisterSeqNum is required")
    private Integer auditRegisterSeqNum;

    @NotNull(message = "businessDate is required")
    private LocalDate businessDate;

    @NotEmpty(message = "auditRegisterEntries cannot be empty")
    @Valid
    private List<AuditRegisterEntry> auditRegisterEntries;
}

