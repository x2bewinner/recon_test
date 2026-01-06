package com.financial.recon.repository;

import java.time.LocalDate;

/**
 * Transaction Total Summary Repository Interface
 * Used to calculate transaction total by executing SELECT query and inserting data
 */
public interface TransactionTotalSummaryRepository {

    /**
     * Calculate transaction total by executing SELECT query and inserting data
     * 
     * @param settlementDate Settlement date
     * @return Execution result, true indicates success, false indicates failure
     */
    boolean callCalculateTransactionTotalProcedure(LocalDate settlementDate);
}

