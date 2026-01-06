package com.xxcards.xbtx.udar.service;

import java.time.LocalDate;

/**
 * UD AR Reconciliation Service Interface
 * Used to handle UD AR reconciliation business logic
 */
public interface UdArReconciliationService {

    /**
     * Execute UD AR reconciliation
     * 
     * @param settlementDate Settlement date
     * @return Execution result, true indicates success, false indicates failure
     */
    boolean executeReconciliation(LocalDate settlementDate);
}

