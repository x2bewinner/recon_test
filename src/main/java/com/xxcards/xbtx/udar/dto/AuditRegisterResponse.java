package com.xxcards.xbtx.udar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditRegisterResponse {
    private String responseCode;
    private String responseMessage;
    private List<String> errors;
}

