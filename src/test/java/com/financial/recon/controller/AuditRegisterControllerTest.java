package com.financial.recon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.recon.dto.AuditRegisterRequest;
import com.financial.recon.dto.AuditRegisterResponse;
import com.financial.recon.util.TestDataBuilder;
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
@DisplayName("審計註冊控制器測試")
class AuditRegisterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // 測試資料會在每個測試方法中創建
    }

    @Test
    @DisplayName("測試基本 API 請求 - 應該返回成功響應")
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

        // 驗證響應內容
        String responseContent = result.getResponse().getContentAsString();
        AuditRegisterResponse response = objectMapper.readValue(responseContent, AuditRegisterResponse.class);
        assertEquals("SUCCESS", response.getResponseCode());
        assertTrue(response.getResponseMessage().contains("Successfully processed"));
    }

    @Test
    @DisplayName("測試缺少必填欄位的請求 - 應該返回驗證錯誤")
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
    @DisplayName("測試空交易列表 - 應該返回驗證錯誤")
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
    @DisplayName("測試空條目列表 - 應該返回驗證錯誤")
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
    @DisplayName("測試批次處理 - 應該成功處理多筆交易")
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
    @DisplayName("測試多條目交易 - 應該成功處理")
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
    @DisplayName("測試可選標頭 - 沒有 X-Client-Request-Identifier 也應該成功")
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
    @DisplayName("測試設備重啟場景 - 應該成功處理")
    void testDeviceRestartScenario() throws Exception {
        // Given
        // 先發送一筆正常交易
        AuditRegisterRequest request1 = TestDataBuilder.createBasicRequest();
        String requestJson1 = objectMapper.writeValueAsString(request1);
        mockMvc.perform(post("/v1/ar/auditRegister")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson1))
                .andExpect(status().isOk());

        // 再發送一筆序列號更大的交易
        AuditRegisterRequest request2 = TestDataBuilder.createRequestForDeviceAndDate(
                "DEVICE-001", java.time.LocalDate.now(), 2);
        String requestJson2 = objectMapper.writeValueAsString(request2);
        mockMvc.perform(post("/v1/ar/auditRegister")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson2))
                .andExpect(status().isOk());

        // 設備重啟後，序列號重置為 1
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
    @DisplayName("測試跨日期交易 - 應該成功處理")
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
    @DisplayName("測試無效 JSON - 應該返回錯誤")
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
    @DisplayName("測試缺少 Content-Type - 應該返回錯誤")
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

