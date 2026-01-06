package com.financial.recon.repository;

import java.time.LocalDate;

/**
 * UD AR Reconciliation Repository Interface
 * Used to execute UD AR reconciliation by executing SELECT query and inserting data
 */
public interface UdArReconciliationRepository {

    /**
     * Execute UD AR reconciliation by executing SELECT query and inserting data
     * 
     * @param settlementDate Settlement date
     * @return Execution result, true indicates success, false indicates failure
     */
    boolean callReconciliationProcedure(LocalDate settlementDate);
}

