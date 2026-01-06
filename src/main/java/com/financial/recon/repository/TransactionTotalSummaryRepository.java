package com.financial.recon.repository;

import com.financial.recon.entity.TransactionTotal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Transaction Total Summary Repository Interface
 * Used to calculate transaction total via separate find and insert operations.
 */
@Repository
public interface TransactionTotalSummaryRepository extends JpaRepository<TransactionTotal, Long> {

    /**
     * Projection for aggregated transaction totals from MIRROR_RAW_TXN.
     */
    interface TransactionTotalAggregate {
        LocalDate getSettlementDate();
        String getTxnType();
        String getTxnSubtype();
        Integer getBeId();
        Integer getDebtorBeId();
        Integer getCreditorBeId();
        String getIssuerId();
        String getDeviceId();
        Long getUdSettleCount();
        BigDecimal getUdSettleAmount();
        Long getUdNotSettleCount();
        BigDecimal getUdNotSettleAmount();
        LocalDate getBeBusinessDate();
        String getProductCode();
        BigDecimal getApportionmentValue();
    }

    /**
     * Find aggregated transaction totals from MIRROR_RAW_TXN for the given settlement date.
     *
     * @param settlementDate Settlement date
     * @return list of aggregated transaction totals
     */
    @Query(value = """
            SELECT 
                SETTLEMENT_DATE,
                TXN_TYPE,
                TXN_SUBTYPE,
                BE_ID,
                DEBTOR_BE_ID,
                CREDITOR_BE_ID,
                ISSUER_ID,
                DEVICE_ID,
                SUM(CASE WHEN SETTLEMENT_STATUS = 'SETTLED' THEN 1 ELSE 0 END) AS UD_SETTLE_COUNT,
                SUM(CASE WHEN SETTLEMENT_STATUS = 'SETTLED' THEN TXN_AMOUNT ELSE 0 END) AS UD_SETTLE_AMOUNT,
                SUM(CASE WHEN SETTLEMENT_STATUS != 'SETTLED' OR SETTLEMENT_STATUS IS NULL THEN 1 ELSE 0 END) AS UD_NOT_SETTLE_COUNT,
                SUM(CASE WHEN SETTLEMENT_STATUS != 'SETTLED' OR SETTLEMENT_STATUS IS NULL THEN TXN_AMOUNT ELSE 0 END) AS UD_NOT_SETTLE_AMOUNT,
                BE_BUSINESS_DATE,
                PRODUCT_CODE,
                APPORTIONMENT_VALUE
            FROM MIRROR_RAW_TXN
            WHERE SETTLEMENT_DATE = :settlementDate
            GROUP BY DEVICE_ID, TXN_TYPE, TXN_SUBTYPE, BE_BUSINESS_DATE, SETTLEMENT_DATE,
                     BE_ID, DEBTOR_BE_ID, CREDITOR_BE_ID, ISSUER_ID, PRODUCT_CODE, APPORTIONMENT_VALUE
            """, nativeQuery = true)
    List<TransactionTotalAggregate> findTransactionTotalAggregates(
            @Param("settlementDate") LocalDate settlementDate);

}


