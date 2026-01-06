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
 * UD AR Reconciliation Entity
 * Represents UD AR reconciliation results
 */
@Entity
@Table(name = "UD_AR_RECONCILIATION")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UdArReconciliation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "BE_ID", precision = 10, scale = 0)
    private Integer beId;

    @Column(name = "SETTLEMENT_DATE", nullable = false)
    private LocalDate settlementDate;

    @Column(name = "AR_TYPE_IDENTIFIER", length = 20)
    private String arTypeIdentifier;

    @Column(name = "CARD_MEDIA_TYPE_ID", precision = 10, scale = 0)
    private Integer cardMediaTypeId;

    @Column(name = "TRANSACTION_COUNT", precision = 10, scale = 0)
    private Integer transactionCount;

    @Column(name = "TOTAL_COUNT", precision = 19, scale = 0)
    private Long totalCount;

    @Column(name = "TOTAL_VALUE", precision = 19, scale = 2)
    private BigDecimal totalValue;

    @Column(name = "DEVICE_COUNT", precision = 10, scale = 0)
    private Integer deviceCount;

    @Column(name = "RECONCILIATION_STATUS", length = 20)
    private String reconciliationStatus;

    @Column(name = "CREATED_TIME")
    private OffsetDateTime createdTime;

    @Column(name = "LAST_UPDATED_TIME")
    private OffsetDateTime lastUpdatedTime;
}

