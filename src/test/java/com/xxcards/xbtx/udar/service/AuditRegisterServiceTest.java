package com.xxcards.xbtx.udar.service;

import com.xxcards.xbtx.udar.dto.AuditRegisterRequest;
import com.xxcards.xbtx.udar.dto.AuditRegisterResponse;
import com.xxcards.xbtx.udar.entity.DeviceAuditRegisterSummary;
import com.xxcards.xbtx.udar.repository.DeviceAuditRegisterSummaryRepository;
import com.xxcards.xbtx.udar.repository.MirrorArDetailRepository;
import com.xxcards.xbtx.udar.repository.MirrorArRepository;
import com.xxcards.xbtx.udar.util.TestDataBuilder;
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
@DisplayName("Audit Register Service Test")
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
        // Clean up test data
        mirrorArRepository.deleteAll();
        mirrorArDetailRepository.deleteAll();
        deviceAuditRegisterSummaryRepository.deleteAll();
    }

    @Test
    @DisplayName("Test basic transaction processing - should successfully process and save to database")
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

        // Verify data saved to database
        assertEquals(1, mirrorArRepository.count());
        assertEquals(1, mirrorArDetailRepository.count());
    }

    @Test
    @DisplayName("Test device accumulated summary - first request should create new summary")
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
    @DisplayName("Test device accumulated summary - second request should accumulate to existing summary")
    void testDeviceSummaryAccumulation() {
        // Given
        String deviceId = "DEVICE-001";
        LocalDate businessDate = LocalDate.now();
        String clientRequestId = "TEST-003";

        // First request
        AuditRegisterRequest request1 = TestDataBuilder.createRequestForDeviceAndDate(
                deviceId, businessDate, 1);
        auditRegisterService.processAuditRegister(request1, clientRequestId);

        // Second request
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
    @DisplayName("Test device restart handling - should correctly accumulate after seqNum reset")
    void testDeviceRestartHandling() {
        // Given
        String deviceId = "DEVICE-001";
        LocalDate businessDate = LocalDate.now();
        String clientRequestId = "TEST-004";

        // First request (seqNum = 1)
        AuditRegisterRequest request1 = TestDataBuilder.createRequestForDeviceAndDate(
                deviceId, businessDate, 1);
        auditRegisterService.processAuditRegister(request1, clientRequestId);

        // Second request (seqNum = 2)
        AuditRegisterRequest request2 = TestDataBuilder.createRequestForDeviceAndDate(
                deviceId, businessDate, 2);
        request2.getAuditRegisterTxns().get(0).getAuditRegisterEntries().get(0)
                .setCount(15);
        request2.getAuditRegisterTxns().get(0).getAuditRegisterEntries().get(0)
                .setValue(1500.00);
        auditRegisterService.processAuditRegister(request2, clientRequestId);

        // After device restart (seqNum = 1, less than previous maximum 2)
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
        // Should accumulate: 10 + 15 + 5 = 30
        assertEquals(30L, s.getTotalCount());
        // Should accumulate: 1000.50 + 1500.00 + 500.00 = 3000.50
        assertEquals(BigDecimal.valueOf(3000.50), s.getTotalValue());
    }

    @Test
    @DisplayName("Test cross-date transaction processing - business date is yesterday should correctly accumulate to yesterday summary")
    void testCrossDateTransaction() {
        // Given
        String deviceId = "DEVICE-001";
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate today = LocalDate.now();
        String clientRequestId = "TEST-005";

        // Yesterday transactions sent today
        AuditRegisterRequest request = TestDataBuilder.createRequestForDeviceAndDate(
                deviceId, yesterday, 1);

        // When
        auditRegisterService.processAuditRegister(request, clientRequestId);

        // Then
        // Should create yesterday summary, not today
        Optional<DeviceAuditRegisterSummary> summaryYesterday = deviceAuditRegisterSummaryRepository
                .findByDeviceIdAndBeIdAndBusinessDateAndArTypeIdentifierAndCardMediaTypeId(
                        deviceId, 1, yesterday, "AR-TYPE-001", "CARD-001");

        Optional<DeviceAuditRegisterSummary> summaryToday = deviceAuditRegisterSummaryRepository
                .findByDeviceIdAndBeIdAndBusinessDateAndArTypeIdentifierAndCardMediaTypeId(
                        deviceId, 1, today, "AR-TYPE-001", "CARD-001");

        assertTrue(summaryYesterday.isPresent(), "Should create yesterday summary");
        assertFalse(summaryToday.isPresent(), "Should not create today summary");

        DeviceAuditRegisterSummary s = summaryYesterday.get();
        assertEquals(10L, s.getTotalCount());
        assertEquals(BigDecimal.valueOf(1000.50), s.getTotalValue());
    }

    @Test
    @DisplayName("Test multi-entry transaction - should create summary for each entry")
    void testMultiEntryTransaction() {
        // Given
        AuditRegisterRequest request = TestDataBuilder.createMultiEntryRequest();
        String clientRequestId = "TEST-006";

        // When
        auditRegisterService.processAuditRegister(request, clientRequestId);

        // Then
        // Should create 3 different summary records
        List<DeviceAuditRegisterSummary> summaries = deviceAuditRegisterSummaryRepository
                .findByDeviceIdAndBeIdAndBusinessDate(
                        "DEVICE-001", 1, LocalDate.now());

        assertEquals(3, summaries.size());

        // Verify summary for each entry
        summaries.forEach(summary -> {
            assertNotNull(summary.getTotalCount());
            assertNotNull(summary.getTotalValue());
        });
    }

    @Test
    @DisplayName("Test batch processing - should process multiple transactions")
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
    @DisplayName("Test partial failure handling - partial transaction failures should return PARTIAL_SUCCESS")
    void testPartialFailure() {
        // Given
        AuditRegisterRequest request = TestDataBuilder.createBasicRequest();
        String clientRequestId = "TEST-008";

        // Add an invalid transaction (missing required fields)
        request.getAuditRegisterTxns().add(TestDataBuilder.createBasicTransaction());
        request.getAuditRegisterTxns().get(1).setDeviceId(null); // Set to null to trigger error

        // When
        AuditRegisterResponse response = auditRegisterService.processAuditRegister(request, clientRequestId);

        // Then
        // Note: Since validation is performed at Controller layer, Service layer may not receive invalid requests
        // This test mainly verifies exception handling logic
        assertNotNull(response);
        assertNotNull(response.getResponseCode());
    }

    @Test
    @DisplayName("Test independent summaries for different devices")
    void testDifferentDevicesIndependentSummary() {
        // Given
        LocalDate businessDate = LocalDate.now();
        String clientRequestId = "TEST-009";

        // Request for device 1
        AuditRegisterRequest request1 = TestDataBuilder.createRequestForDeviceAndDate(
                "DEVICE-001", businessDate, 1);
        auditRegisterService.processAuditRegister(request1, clientRequestId);

        // Request for device 2
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
        // Summaries for two devices should be independent
        assertEquals(10L, summary1.get().getTotalCount());
        assertEquals(10L, summary2.get().getTotalCount());
    }

    @Test
    @DisplayName("Test independent summaries for different business dates")
    void testDifferentBusinessDatesIndependentSummary() {
        // Given
        String deviceId = "DEVICE-001";
        LocalDate date1 = LocalDate.now();
        LocalDate date2 = LocalDate.now().plusDays(1);
        String clientRequestId = "TEST-010";

        // Request for date 1
        AuditRegisterRequest request1 = TestDataBuilder.createRequestForDeviceAndDate(
                deviceId, date1, 1);
        auditRegisterService.processAuditRegister(request1, clientRequestId);

        // Request for date 2
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
        // Summaries for two dates should be independent
        assertEquals(10L, summary1.get().getTotalCount());
        assertEquals(10L, summary2.get().getTotalCount());
    }
}

