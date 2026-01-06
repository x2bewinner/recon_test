package com.xxcards.xbtx.udar.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Transaction Total Entity
 * Represents aggregated transaction totals from MIRROR_RAW_TXN
 */
@Entity
@Table(name = "TRANSACTION_TOTAL")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionTotal {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "SETTLEMENT_DATE", nullable = false)
    private LocalDate settlementDate;

    @Column(name = "TXN_TYPE", length = 3)
    private String txnType;

    @Column(name = "TXN_SUBTYPE", length = 3)
    private String txnSubtype;

    @Column(name = "BE_ID", precision = 10, scale = 0)
    private Integer beId;

    @Column(name = "DEBTOR_BE_ID", precision = 10, scale = 0)
    private Integer debtorBeId;

    @Column(name = "CREDITOR_BE_ID", precision = 10, scale = 0)
    private Integer creditorBeId;

    @Column(name = "ISSUER_ID", length = 20)
    private String issuerId;

    @Column(name = "DEVICE_ID", length = 20)
    private String deviceId;

    @Column(name = "UD_SETTLE_COUNT", precision = 19, scale = 0)
    private Long udSettleCount;

    @Column(name = "UD_SETTLE_AMOUNT", precision = 19, scale = 2)
    private BigDecimal udSettleAmount;

    @Column(name = "UD_NOT_SETTLE_COUNT", precision = 19, scale = 0)
    private Long udNotSettleCount;

    @Column(name = "UD_NOT_SETTLE_AMOUNT", precision = 19, scale = 2)
    private BigDecimal udNotSettleAmount;

    @Column(name = "BE_BUSINESS_DATE")
    private LocalDate beBusinessDate;

    @Column(name = "PRODUCT_CODE", length = 20)
    private String productCode;

    @Column(name = "APPORTIONMENT_VALUE", precision = 19, scale = 2)
    private BigDecimal apportionmentValue;
}

