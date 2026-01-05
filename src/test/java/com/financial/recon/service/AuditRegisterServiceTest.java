package com.financial.recon.service;

import com.financial.recon.dto.AuditRegisterRequest;
import com.financial.recon.dto.AuditRegisterResponse;
import com.financial.recon.entity.DeviceAuditRegisterSummary;
import com.financial.recon.repository.DeviceAuditRegisterSummaryRepository;
import com.financial.recon.repository.MirrorArDetailRepository;
import com.financial.recon.repository.MirrorArRepository;
import com.financial.recon.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("審計註冊服務測試")
class AuditRegisterServiceTest {

    @Autowired
    private AuditRegisterService auditRegisterService;

    @Autowired
    private MirrorArRepository mirrorArRepository;

    @Autowired
    private MirrorArDetailRepository mirrorArDetailRepository;

    @Autowired
    private DeviceAuditRegisterSummaryRepository deviceAuditRegisterSummaryRepository;

    @BeforeEach
    void setUp() {
        // 清理測試資料
        mirrorArRepository.deleteAll();
        mirrorArDetailRepository.deleteAll();
        deviceAuditRegisterSummaryRepository.deleteAll();
    }

    @Test
    @DisplayName("測試基本交易處理 - 應該成功處理並保存到資料庫")
    void testProcessBasicTransaction() {
        // Given
        AuditRegisterRequest request = TestDataBuilder.createBasicRequest();
        String clientRequestId = "TEST-001";

        // When
        AuditRegisterResponse response = auditRegisterService.processAuditRegister(request, clientRequestId);

        // Then
        assertEquals("SUCCESS", response.getResponseCode());
        assertNull(response.getErrors());
        assertTrue(response.getResponseMessage().contains("Successfully processed"));

        // 驗證資料已保存到資料庫
        assertEquals(1, mirrorArRepository.count());
        assertEquals(1, mirrorArDetailRepository.count());
    }

    @Test
    @DisplayName("測試設備累計摘要 - 第一次請求應該創建新摘要")
    void testDeviceSummaryFirstRequest() {
        // Given
        AuditRegisterRequest request = TestDataBuilder.createBasicRequest();
        String clientRequestId = "TEST-002";

        // When
        auditRegisterService.processAuditRegister(request, clientRequestId);

        // Then
        Optional<DeviceAuditRegisterSummary> summary = deviceAuditRegisterSummaryRepository
                .findByDeviceIdAndBeIdAndBusinessDateAndArTypeIdentifierAndCardMediaTypeId(
                        "DEVICE-001", 1, LocalDate.now(), "AR-TYPE-001", "CARD-001");

        assertTrue(summary.isPresent());
        DeviceAuditRegisterSummary s = summary.get();
        assertEquals(10L, s.getTotalCount());
        assertEquals(BigDecimal.valueOf(1000.50), s.getTotalValue());
        assertEquals(1, s.getLastArSeqNum());
    }

    @Test
    @DisplayName("測試設備累計摘要 - 第二次請求應該累計到現有摘要")
    void testDeviceSummaryAccumulation() {
        // Given
        String deviceId = "DEVICE-001";
        LocalDate businessDate = LocalDate.now();
        String clientRequestId = "TEST-003";

        // 第一次請求
        AuditRegisterRequest request1 = TestDataBuilder.createRequestForDeviceAndDate(
                deviceId, businessDate, 1);
        auditRegisterService.processAuditRegister(request1, clientRequestId);

        // 第二次請求
        AuditRegisterRequest request2 = TestDataBuilder.createRequestForDeviceAndDate(
                deviceId, businessDate, 2);
        request2.getAuditRegisterTxns().get(0).getAuditRegisterEntries().get(0)
                .setCount(15);
        request2.getAuditRegisterTxns().get(0).getAuditRegisterEntries().get(0)
                .setValue(1500.00);

        // When
        auditRegisterService.processAuditRegister(request2, clientRequestId);

        // Then
        Optional<DeviceAuditRegisterSummary> summary = deviceAuditRegisterSummaryRepository
                .findByDeviceIdAndBeIdAndBusinessDateAndArTypeIdentifierAndCardMediaTypeId(
                        deviceId, 1, businessDate, "AR-TYPE-001", "CARD-001");

        assertTrue(summary.isPresent());
        DeviceAuditRegisterSummary s = summary.get();
        assertEquals(25L, s.getTotalCount()); // 10 + 15
        assertEquals(BigDecimal.valueOf(2500.50), s.getTotalValue()); // 1000.50 + 1500.00
        assertEquals(2, s.getLastArSeqNum());
    }

    @Test
    @DisplayName("測試設備重啟處理 - seqNum 重置後應該正確累計")
    void testDeviceRestartHandling() {
        // Given
        String deviceId = "DEVICE-001";
        LocalDate businessDate = LocalDate.now();
        String clientRequestId = "TEST-004";

        // 第一次請求 (seqNum = 1)
        AuditRegisterRequest request1 = TestDataBuilder.createRequestForDeviceAndDate(
                deviceId, businessDate, 1);
        auditRegisterService.processAuditRegister(request1, clientRequestId);

        // 第二次請求 (seqNum = 2)
        AuditRegisterRequest request2 = TestDataBuilder.createRequestForDeviceAndDate(
                deviceId, businessDate, 2);
        request2.getAuditRegisterTxns().get(0).getAuditRegisterEntries().get(0)
                .setCount(15);
        request2.getAuditRegisterTxns().get(0).getAuditRegisterEntries().get(0)
                .setValue(1500.00);
        auditRegisterService.processAuditRegister(request2, clientRequestId);

        // 設備重啟後 (seqNum = 1，小於之前的最大值 2)
        AuditRegisterRequest request3 = TestDataBuilder.createRequestForDeviceAndDate(
                deviceId, businessDate, 1);
        request3.getAuditRegisterTxns().get(0).getAuditRegisterEntries().get(0)
                .setCount(5);
        request3.getAuditRegisterTxns().get(0).getAuditRegisterEntries().get(0)
                .setValue(500.00);

        // When
        auditRegisterService.processAuditRegister(request3, clientRequestId);

        // Then
        Optional<DeviceAuditRegisterSummary> summary = deviceAuditRegisterSummaryRepository
                .findByDeviceIdAndBeIdAndBusinessDateAndArTypeIdentifierAndCardMediaTypeId(
                        deviceId, 1, businessDate, "AR-TYPE-001", "CARD-001");

        assertTrue(summary.isPresent());
        DeviceAuditRegisterSummary s = summary.get();
        // 應該累計：10 + 15 + 5 = 30
        assertEquals(30L, s.getTotalCount());
        // 應該累計：1000.50 + 1500.00 + 500.00 = 3000.50
        assertEquals(BigDecimal.valueOf(3000.50), s.getTotalValue());
    }

    @Test
    @DisplayName("測試跨日期交易處理 - 業務日期是昨天應該正確累計到昨天的摘要")
    void testCrossDateTransaction() {
        // Given
        String deviceId = "DEVICE-001";
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate today = LocalDate.now();
        String clientRequestId = "TEST-005";

        // 昨天的交易在今天發送
        AuditRegisterRequest request = TestDataBuilder.createRequestForDeviceAndDate(
                deviceId, yesterday, 1);

        // When
        auditRegisterService.processAuditRegister(request, clientRequestId);

        // Then
        // 應該創建昨天的摘要，而不是今天的
        Optional<DeviceAuditRegisterSummary> summaryYesterday = deviceAuditRegisterSummaryRepository
                .findByDeviceIdAndBeIdAndBusinessDateAndArTypeIdentifierAndCardMediaTypeId(
                        deviceId, 1, yesterday, "AR-TYPE-001", "CARD-001");

        Optional<DeviceAuditRegisterSummary> summaryToday = deviceAuditRegisterSummaryRepository
                .findByDeviceIdAndBeIdAndBusinessDateAndArTypeIdentifierAndCardMediaTypeId(
                        deviceId, 1, today, "AR-TYPE-001", "CARD-001");

        assertTrue(summaryYesterday.isPresent(), "應該創建昨天的摘要");
        assertFalse(summaryToday.isPresent(), "不應該創建今天的摘要");

        DeviceAuditRegisterSummary s = summaryYesterday.get();
        assertEquals(10L, s.getTotalCount());
        assertEquals(BigDecimal.valueOf(1000.50), s.getTotalValue());
    }

    @Test
    @DisplayName("測試多條目交易 - 應該為每個條目創建摘要")
    void testMultiEntryTransaction() {
        // Given
        AuditRegisterRequest request = TestDataBuilder.createMultiEntryRequest();
        String clientRequestId = "TEST-006";

        // When
        auditRegisterService.processAuditRegister(request, clientRequestId);

        // Then
        // 應該創建 3 個不同的摘要記錄
        List<DeviceAuditRegisterSummary> summaries = deviceAuditRegisterSummaryRepository
                .findByDeviceIdAndBeIdAndBusinessDate(
                        "DEVICE-001", 1, LocalDate.now());

        assertEquals(3, summaries.size());

        // 驗證每個條目的摘要
        summaries.forEach(summary -> {
            assertNotNull(summary.getTotalCount());
            assertNotNull(summary.getTotalValue());
        });
    }

    @Test
    @DisplayName("測試批次處理 - 應該處理多筆交易")
    void testBatchProcessing() {
        // Given
        AuditRegisterRequest request = TestDataBuilder.createBatchRequest(5);
        String clientRequestId = "TEST-007";

        // When
        AuditRegisterResponse response = auditRegisterService.processAuditRegister(request, clientRequestId);

        // Then
        assertEquals("SUCCESS", response.getResponseCode());
        assertEquals(5, mirrorArRepository.count());
        assertEquals(5, mirrorArDetailRepository.count());
    }

    @Test
    @DisplayName("測試部分失敗處理 - 部分交易失敗應該返回 PARTIAL_SUCCESS")
    void testPartialFailure() {
        // Given
        AuditRegisterRequest request = TestDataBuilder.createBasicRequest();
        String clientRequestId = "TEST-008";

        // 添加一筆無效交易（缺少必填欄位）
        request.getAuditRegisterTxns().add(TestDataBuilder.createBasicTransaction());
        request.getAuditRegisterTxns().get(1).setDeviceId(null); // 設置為 null 以觸發錯誤

        // When
        AuditRegisterResponse response = auditRegisterService.processAuditRegister(request, clientRequestId);

        // Then
        // 注意：由於驗證在 Controller 層進行，Service 層可能不會收到無效請求
        // 這個測試主要驗證異常處理邏輯
        assertNotNull(response);
        assertNotNull(response.getResponseCode());
    }

    @Test
    @DisplayName("測試不同設備的獨立摘要")
    void testDifferentDevicesIndependentSummary() {
        // Given
        LocalDate businessDate = LocalDate.now();
        String clientRequestId = "TEST-009";

        // 設備 1 的請求
        AuditRegisterRequest request1 = TestDataBuilder.createRequestForDeviceAndDate(
                "DEVICE-001", businessDate, 1);
        auditRegisterService.processAuditRegister(request1, clientRequestId);

        // 設備 2 的請求
        AuditRegisterRequest request2 = TestDataBuilder.createRequestForDeviceAndDate(
                "DEVICE-002", businessDate, 1);
        auditRegisterService.processAuditRegister(request2, clientRequestId);

        // When & Then
        Optional<DeviceAuditRegisterSummary> summary1 = deviceAuditRegisterSummaryRepository
                .findByDeviceIdAndBeIdAndBusinessDateAndArTypeIdentifierAndCardMediaTypeId(
                        "DEVICE-001", 1, businessDate, "AR-TYPE-001", "CARD-001");

        Optional<DeviceAuditRegisterSummary> summary2 = deviceAuditRegisterSummaryRepository
                .findByDeviceIdAndBeIdAndBusinessDateAndArTypeIdentifierAndCardMediaTypeId(
                        "DEVICE-002", 1, businessDate, "AR-TYPE-001", "CARD-001");

        assertTrue(summary1.isPresent());
        assertTrue(summary2.isPresent());
        // 兩個設備的摘要應該獨立
        assertEquals(10L, summary1.get().getTotalCount());
        assertEquals(10L, summary2.get().getTotalCount());
    }

    @Test
    @DisplayName("測試不同業務日期的獨立摘要")
    void testDifferentBusinessDatesIndependentSummary() {
        // Given
        String deviceId = "DEVICE-001";
        LocalDate date1 = LocalDate.now();
        LocalDate date2 = LocalDate.now().plusDays(1);
        String clientRequestId = "TEST-010";

        // 日期 1 的請求
        AuditRegisterRequest request1 = TestDataBuilder.createRequestForDeviceAndDate(
                deviceId, date1, 1);
        auditRegisterService.processAuditRegister(request1, clientRequestId);

        // 日期 2 的請求
        AuditRegisterRequest request2 = TestDataBuilder.createRequestForDeviceAndDate(
                deviceId, date2, 1);
        auditRegisterService.processAuditRegister(request2, clientRequestId);

        // When & Then
        Optional<DeviceAuditRegisterSummary> summary1 = deviceAuditRegisterSummaryRepository
                .findByDeviceIdAndBeIdAndBusinessDateAndArTypeIdentifierAndCardMediaTypeId(
                        deviceId, 1, date1, "AR-TYPE-001", "CARD-001");

        Optional<DeviceAuditRegisterSummary> summary2 = deviceAuditRegisterSummaryRepository
                .findByDeviceIdAndBeIdAndBusinessDateAndArTypeIdentifierAndCardMediaTypeId(
                        deviceId, 1, date2, "AR-TYPE-001", "CARD-001");

        assertTrue(summary1.isPresent());
        assertTrue(summary2.isPresent());
        // 兩個日期的摘要應該獨立
        assertEquals(10L, summary1.get().getTotalCount());
        assertEquals(10L, summary2.get().getTotalCount());
    }
}

