package com.financial.recon.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * UD AR Reconciliation Repository Interface
 * Used to execute UD AR reconciliation by executing a native SQL INSERT...SELECT
 */
@Repository
public interface UdArReconciliationRepository {

    /**
     * Insert UD AR reconciliation results into UD_AR_RECONCILIATION table
     * by aggregating data from MIRROR_AR and MIRROR_AR_DETAIL for the given date.
     *
     * @param settlementDate Settlement date (used as BUSINESS_DATE filter for MIRROR_AR)
     * @return number of rows inserted
     */
    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO UD_AR_RECONCILIATION (
                BE_ID,
                SETTLEMENT_DATE,
                AR_TYPE_IDENTIFIER,
                CARD_MEDIA_TYPE_ID,
                TRANSACTION_COUNT,
                TOTAL_COUNT,
                TOTAL_VALUE,
                DEVICE_COUNT,
                RECONCILIATION_STATUS,
                CREATED_TIME,
                LAST_UPDATED_TIME
            )
            SELECT 
                MA.BE_ID,
                :settlementDate AS SETTLEMENT_DATE,
                MAD.AR_ID AS AR_TYPE_IDENTIFIER,
                MAD.ID_TYPE AS CARD_MEDIA_TYPE_ID,
                COUNT(*) AS TRANSACTION_COUNT,
                SUM(MAD.COUNT) AS TOTAL_COUNT,
                SUM(MAD.VALUE) AS TOTAL_VALUE,
                COUNT(DISTINCT MA.DEVICE_ID) AS DEVICE_COUNT,
                'SUCCESS' AS RECONCILIATION_STATUS,
                CURRENT_TIMESTAMP AS CREATED_TIME,
                CURRENT_TIMESTAMP AS LAST_UPDATED_TIME
            FROM MIRROR_AR MA
            INNER JOIN MIRROR_AR_DETAIL MAD ON MA.REFERENCE_ID = MAD.REFERENCE_ID
            WHERE MA.BUSINESS_DATE = :settlementDate
            GROUP BY MA.BE_ID, MA.BUSINESS_DATE, MAD.AR_ID, MAD.ID_TYPE
            """, nativeQuery = true)
    int insertUdArReconciliationResults(@Param("settlementDate") LocalDate settlementDate);
}

