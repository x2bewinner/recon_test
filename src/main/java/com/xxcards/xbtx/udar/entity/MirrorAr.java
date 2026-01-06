package com.xxcards.xbtx.udar.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "MIRROR_AR")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MirrorAr {
    @Id
    @Column(name = "REFERENCE_ID", length = 64)
    private String referenceId;

    @Column(name = "REF_RECORD_ID", length = 5)
    private String refRecordId;

    @Column(name = "REF_TOTAL_COUNT", precision = 10, scale = 0)
    private Integer refTotalCount;

    @Column(name = "TXN_TYPE", length = 3, nullable = false)
    private String txnType;

    @Column(name = "TXN_SUBTYPE", length = 3, nullable = false)
    private String txnSubtype;

    @Column(name = "END_TXN_TIME", nullable = false)
    private OffsetDateTime endTxnTime;

    @Column(name = "UDSN", length = 64, nullable = false)
    private String udsn;

    @Column(name = "DEVICE_ID", length = 20, nullable = false)
    private String deviceId;

    @Column(name = "HARDWARE_TYPE", length = 8)
    private String hardwareType;

    @Column(name = "SERVICE_MODE", length = 3)
    private String serviceMode;

    @Column(name = "LOCATION_ID", length = 10)
    private String locationId;

    @Column(name = "TARIFF_LOCATION_ID", length = 10)
    private String tariffLocationId;

    @Column(name = "STAFF_ID", length = 10)
    private String staffId;

    @Column(name = "SHIFT_ID", length = 10)
    private String shiftId;

    @Column(name = "BE_ID", precision = 10, scale = 0)
    private Integer beId;

    @Column(name = "TEST_MODE", precision = 1, scale = 0)
    private Integer testMode;

    @Column(name = "LATITUDE", length = 15)
    private String latitude;

    @Column(name = "LONGITUDE", length = 15)
    private String longitude;

    @Column(name = "TARIFF_VERSION_ID", length = 20)
    private String tariffVersionId;

    @Column(name = "LOCATION_TYPE", length = 10)
    private String locationType;

    @Column(name = "DEVICE_SUB_ID", length = 10)
    private String deviceSubId;

    @Column(name = "STAFF_TYPE", length = 15)
    private String staffType;

    @Column(name = "FACILITY_OPER_ID", precision = 10, scale = 0)
    private Integer facilityOperId;

    @Column(name = "PHYSICAL_DEVICE_ID", length = 20)
    private String physicalDeviceId;

    @Column(name = "READER_ID", length = 64)
    private String readerId;

    @Column(name = "AUDIT_REGISTER_SEQ_NUM", precision = 10, scale = 0)
    private Integer auditRegisterSeqNum;

    @Column(name = "BUSINESS_DATE")
    private OffsetDateTime businessDate;

    @Column(name = "SETTLEMENT_DATE", nullable = false)
    private OffsetDateTime settlementDate;

    @Column(name = "RECEIVED_TIME")
    private OffsetDateTime receivedTime;

    @Column(name = "LAST_UPDATED_TIME")
    private OffsetDateTime lastUpdatedTime;
}

