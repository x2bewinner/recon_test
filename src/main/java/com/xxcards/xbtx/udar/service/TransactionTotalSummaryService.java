package com.xxcards.xbtx.udar.service;

import java.time.LocalDate;

/**
 * Transaction Total Summary Service Interface
 * Used to calculate transaction total
 */
public interface TransactionTotalSummaryService {

    /**
     * Calculate transaction total
     * 
     * @param settlementDate Settlement date
     * @return Execution result, true indicates success, false indicates failure
     */
    boolean calculateTransactionTotal(LocalDate settlementDate);
}

