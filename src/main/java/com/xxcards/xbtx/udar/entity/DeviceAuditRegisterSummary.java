package com.xxcards.xbtx.udar.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 設備審計註冊摘要表
 * 用於追蹤每個設備在每個業務日期的累計交易計數和金額
 * 處理設備重啟後計數重置的情況
 */
@Entity
@Table(name = "DEVICE_AR_SUMMARY", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"DEVICE_ID", "BE_ID", "BUSINESS_DATE", "AR_TYPE_IDENTIFIER", "CARD_MEDIA_TYPE_ID"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(DeviceAuditRegisterSummaryId.class)
public class DeviceAuditRegisterSummary {
    @Id
    @Column(name = "DEVICE_ID", length = 20, nullable = false)
    private String deviceId;

    @Id
    @Column(name = "BE_ID", precision = 10, scale = 0, nullable = false)
    private Integer beId;

    @Id
    @Column(name = "BUSINESS_DATE", nullable = false)
    private LocalDate businessDate;

    @Id
    @Column(name = "AR_TYPE_IDENTIFIER", length = 20, nullable = false)
    private String arTypeIdentifier;

    @Id
    @Column(name = "CARD_MEDIA_TYPE_ID", length = 20)
    private String cardMediaTypeId;

    @Column(name = "TOTAL_COUNT", precision = 19, scale = 0, nullable = false)
    private Long totalCount;

    @Column(name = "TOTAL_VALUE", precision = 19, scale = 2)
    private BigDecimal totalValue;

    @Column(name = "LAST_AR_SEQ_NUM", precision = 10, scale = 0)
    private Integer lastArSeqNum;

    @Column(name = "LAST_UPDATED_TIME", nullable = false)
    private OffsetDateTime lastUpdatedTime;

    @Column(name = "CREATED_TIME", nullable = false)
    private OffsetDateTime createdTime;
}

