package com.financial.recon.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AuditRegisterRequest {
    @NotEmpty(message = "auditRegisterTxns cannot be empty")
    @Valid
    private List<AuditRegisterTransaction> auditRegisterTxns;
}

