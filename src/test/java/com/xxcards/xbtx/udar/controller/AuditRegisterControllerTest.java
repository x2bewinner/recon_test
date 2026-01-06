package com.xxcards.xbtx.udar.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxcards.xbtx.udar.dto.AuditRegisterRequest;
import com.xxcards.xbtx.udar.dto.AuditRegisterResponse;
import com.xxcards.xbtx.udar.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Audit Register Controller Test")
class AuditRegisterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Test data will be created in each test method
    }

    @Test
    @DisplayName("Test basic API request - should return success response")
    void testBasicApiRequest() throws Exception {
        // Given
        AuditRegisterRequest request = TestDataBuilder.createBasicRequest();
        String requestJson = objectMapper.writeValueAsString(request);
        String clientRequestId = "TEST-CLIENT-001";

        // When & Then
        MvcResult result = mockMvc.perform(post("/v1/ar/auditRegister")
                        .header("X-Client-Request-Identifier", clientRequestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value("SUCCESS"))
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andReturn();

        // Verify response content
        String responseContent = result.getResponse().getContentAsString();
        AuditRegisterResponse response = objectMapper.readValue(responseContent, AuditRegisterResponse.class);
        assertEquals("SUCCESS", response.getResponseCode());
        assertTrue(response.getResponseMessage().contains("Successfully processed"));
    }

    @Test
    @DisplayName("Test request with missing required fields - should return validation error")
    void testValidationError() throws Exception {
        // Given
        AuditRegisterRequest request = TestDataBuilder.createInvalidRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/v1/ar/auditRegister")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("Test empty transaction list - should return validation error")
    void testEmptyTransactionList() throws Exception {
        // Given
        AuditRegisterRequest request = new AuditRegisterRequest();
        request.setAuditRegisterTxns(java.util.Collections.emptyList());
        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/v1/ar/auditRegister")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Test empty entries list - should return validation error")
    void testEmptyEntriesList() throws Exception {
        // Given
        AuditRegisterRequest request = TestDataBuilder.createEmptyEntriesRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/v1/ar/auditRegister")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.responseCode").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Test batch processing - should successfully process multiple transactions")
    void testBatchProcessing() throws Exception {
        // Given
        AuditRegisterRequest request = TestDataBuilder.createBatchRequest(3);
        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/v1/ar/auditRegister")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value("SUCCESS"))
                .andExpect(jsonPath("$.responseMessage").value("Successfully processed 3 transaction(s)"));
    }

    @Test
    @DisplayName("Test multi-entry transaction - should successfully process")
    void testMultiEntryTransaction() throws Exception {
        // Given
        AuditRegisterRequest request = TestDataBuilder.createMultiEntryRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/v1/ar/auditRegister")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value("SUCCESS"));
    }

    @Test
    @DisplayName("Test optional header - should succeed without X-Client-Request-Identifier")
    void testOptionalHeader() throws Exception {
        // Given
        AuditRegisterRequest request = TestDataBuilder.createBasicRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/v1/ar/auditRegister")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value("SUCCESS"));
    }

    @Test
    @DisplayName("Test device restart scenario - should successfully process")
    void testDeviceRestartScenario() throws Exception {
        // Given
        // First send a normal transaction
        AuditRegisterRequest request1 = TestDataBuilder.createBasicRequest();
        String requestJson1 = objectMapper.writeValueAsString(request1);
        mockMvc.perform(post("/v1/ar/auditRegister")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson1))
                .andExpect(status().isOk());

        // Then send a transaction with larger sequence number
        AuditRegisterRequest request2 = TestDataBuilder.createRequestForDeviceAndDate(
                "DEVICE-001", java.time.LocalDate.now(), 2);
        String requestJson2 = objectMapper.writeValueAsString(request2);
        mockMvc.perform(post("/v1/ar/auditRegister")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson2))
                .andExpect(status().isOk());

        // After device restart, sequence number reset to 1
        AuditRegisterRequest request3 = TestDataBuilder.createRequestForDeviceAndDate(
                "DEVICE-001", java.time.LocalDate.now(), 1);
        String requestJson3 = objectMapper.writeValueAsString(request3);

        // When & Then
        mockMvc.perform(post("/v1/ar/auditRegister")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value("SUCCESS"));
    }

    @Test
    @DisplayName("Test cross-date transaction - should successfully process")
    void testCrossDateTransaction() throws Exception {
        // Given
        AuditRegisterRequest request = TestDataBuilder.createCrossDateRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/v1/ar/auditRegister")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.responseCode").value("SUCCESS"));
    }

    @Test
    @DisplayName("Test invalid JSON - should return error")
    void testInvalidJson() throws Exception {
        // Given
        String invalidJson = "{ invalid json }";

        // When & Then
        mockMvc.perform(post("/v1/ar/auditRegister")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test missing Content-Type - should return error")
    void testMissingContentType() throws Exception {
        // Given
        AuditRegisterRequest request = TestDataBuilder.createBasicRequest();
        String requestJson = objectMapper.writeValueAsString(request);

        // When & Then
        mockMvc.perform(post("/v1/ar/auditRegister")
                        .content(requestJson))
                .andExpect(status().isUnsupportedMediaType());
    }
}

