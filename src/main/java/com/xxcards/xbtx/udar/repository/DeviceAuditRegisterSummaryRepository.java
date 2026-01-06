package com.xxcards.xbtx.udar.repository;

import com.xxcards.xbtx.udar.entity.DeviceAuditRegisterSummary;
import com.xxcards.xbtx.udar.entity.DeviceAuditRegisterSummaryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceAuditRegisterSummaryRepository extends JpaRepository<DeviceAuditRegisterSummary, DeviceAuditRegisterSummaryId> {

    /**
     * 查詢指定設備在指定業務日期的所有摘要記錄
     */
    List<DeviceAuditRegisterSummary> findByDeviceIdAndBeIdAndBusinessDate(
            String deviceId, Integer beId, LocalDate businessDate);

    /**
     * 查詢指定設備在指定業務日期和AR類型的摘要記錄
     */
    Optional<DeviceAuditRegisterSummary> findByDeviceIdAndBeIdAndBusinessDateAndArTypeIdentifierAndCardMediaTypeId(
            String deviceId, Integer beId, LocalDate businessDate, String arTypeIdentifier, String cardMediaTypeId);

    /**
     * 查詢指定設備的最大AR序列號（用於檢測設備是否重啟）
     */
    @Query("SELECT MAX(d.lastArSeqNum) FROM DeviceAuditRegisterSummary d " +
           "WHERE d.deviceId = :deviceId AND d.beId = :beId AND d.businessDate = :businessDate")
    Optional<Integer> findMaxArSeqNumByDeviceAndDate(
            @Param("deviceId") String deviceId,
            @Param("beId") Integer beId,
            @Param("businessDate") LocalDate businessDate);
}

