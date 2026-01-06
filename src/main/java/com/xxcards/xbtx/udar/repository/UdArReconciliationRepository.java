package com.xxcards.xbtx.udar.repository;

import com.xxcards.xbtx.udar.entity.UdArReconciliation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * UD AR Reconciliation Repository Interface
 * Used to execute UD AR reconciliation via separate find and insert operations.
 */
@Repository
public interface UdArReconciliationRepository extends JpaRepository<UdArReconciliation, Long> {

    /**
     * Projection for aggregated UD AR reconciliation data from MIRROR_AR and MIRROR_AR_DETAIL.
     */
    interface UdArReconciliationAggregate {
        Integer getBeId();
        LocalDate getBusinessDate();
        String getArTypeIdentifier();
        Integer getCardMediaTypeId();
        Integer getTransactionCount();
        Long getTotalCount();
        BigDecimal getTotalValue();
        Integer getDeviceCount();
    }

    /**
     * Find aggregated UD AR reconciliation data for the given date.
     *
     * @param settlementDate Settlement date (used as BUSINESS_DATE filter for MIRROR_AR)
     * @return list of aggregated reconciliation data
     */
    @Query(value = """
            SELECT 
                MA.BE_ID,
                MA.BUSINESS_DATE,
                MAD.AR_ID AS AR_TYPE_IDENTIFIER,
                MAD.ID_TYPE AS CARD_MEDIA_TYPE_ID,
                COUNT(*) AS TRANSACTION_COUNT,
                SUM(MAD.COUNT) AS TOTAL_COUNT,
                SUM(MAD.VALUE) AS TOTAL_VALUE,
                COUNT(DISTINCT MA.DEVICE_ID) AS DEVICE_COUNT
            FROM MIRROR_AR MA
            INNER JOIN MIRROR_AR_DETAIL MAD ON MA.REFERENCE_ID = MAD.REFERENCE_ID
            WHERE MA.BUSINESS_DATE = :settlementDate
            GROUP BY MA.BE_ID, MA.BUSINESS_DATE, MAD.AR_ID, MAD.ID_TYPE
            """, nativeQuery = true)
    List<UdArReconciliationAggregate> findUdArReconciliationAggregates(
            @Param("settlementDate") LocalDate settlementDate);

}


