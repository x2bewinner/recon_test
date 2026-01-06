package com.financial.recon.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Transaction Total Summary Repository Interface
 * Used to calculate transaction total by executing a native SQL INSERT...SELECT
 */
@Repository
public interface TransactionTotalSummaryRepository {

    /**
     * Insert aggregated transaction totals into TRANSACTION_TOTAL table
     * based on data from MIRROR_RAW_TXN for the given settlement date.
     *
     * @param settlementDate Settlement date
     * @return number of rows inserted
     */
    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO TRANSACTION_TOTAL (
                SETTLEMENT_DATE,
                TXN_TYPE,
                TXN_SUBTYPE,
                BE_ID,
                DEBTOR_BE_ID,
                CREDITOR_BE_ID,
                ISSUER_ID,
                DEVICE_ID,
                UD_SETTLE_COUNT,
                UD_SETTLE_AMOUNT,
                UD_NOT_SETTLE_COUNT,
                UD_NOT_SETTLE_AMOUNT,
                BE_BUSINESS_DATE,
                PRODUCT_CODE,
                APPORTIONMENT_VALUE
            )
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
    int insertTransactionTotals(@Param("settlementDate") LocalDate settlementDate);
}

