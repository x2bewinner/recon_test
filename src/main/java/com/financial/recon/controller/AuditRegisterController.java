package com.financial.recon.controller;

import com.financial.recon.dto.AuditRegisterRequest;
import com.financial.recon.dto.AuditRegisterResponse;
import com.financial.recon.service.AuditRegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/ar")
@RequiredArgsConstructor
public class AuditRegisterController {

    private final AuditRegisterService auditRegisterService;

    @PostMapping("/auditRegister")
    public ResponseEntity<AuditRegisterResponse> auditRegister(
            @RequestHeader(value = "X-Client-Request-Identifier", required = false) String clientRequestId,
            @Valid @RequestBody AuditRegisterRequest request) {
        
        log.info("Received audit register request. ClientRequestId: {}, TransactionCount: {}", 
                clientRequestId, request.getAuditRegisterTxns().size());

        AuditRegisterResponse response = auditRegisterService.processAuditRegister(
                request, clientRequestId != null ? clientRequestId : "UNKNOWN");

        HttpStatus status = "SUCCESS".equals(response.getResponseCode()) 
                ? HttpStatus.OK 
                : HttpStatus.ACCEPTED; // 202 for partial success or errors

        return ResponseEntity.status(status)
                .header("X-Client-Request-Identifier", clientRequestId != null ? clientRequestId : "")
                .body(response);
    }
}

