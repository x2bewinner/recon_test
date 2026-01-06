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
     * Query all summary records for specified device on specified business date
     */
    List<DeviceAuditRegisterSummary> findByDeviceIdAndBeIdAndBusinessDate(
            String deviceId, Integer beId, LocalDate businessDate);

    /**
     * Query summary record for specified device on specified business date and AR type
     */
    Optional<DeviceAuditRegisterSummary> findByDeviceIdAndBeIdAndBusinessDateAndArTypeIdentifierAndCardMediaTypeId(
            String deviceId, Integer beId, LocalDate businessDate, String arTypeIdentifier, String cardMediaTypeId);

    /**
     * Query maximum AR sequence number for specified device (used to detect device restart)
     */
    @Query("SELECT MAX(d.lastArSeqNum) FROM DeviceAuditRegisterSummary d " +
           "WHERE d.deviceId = :deviceId AND d.beId = :beId AND d.businessDate = :businessDate")
    Optional<Integer> findMaxArSeqNumByDeviceAndDate(
            @Param("deviceId") String deviceId,
            @Param("beId") Integer beId,
            @Param("businessDate") LocalDate businessDate);
}

