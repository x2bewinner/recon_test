package com.xxcards.xbtx.udar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuditRegisterEntry {
    @NotBlank(message = "arTypeIdentifier is required")
    private String arTypeIdentifier;

    private String cardMediaTypeId;

    @NotNull(message = "count is required")
    private Integer count;

    private Double value;
}

