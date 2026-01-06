package com.financial.recon.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UdArReconciliationRepositoryImpl implements UdArReconciliationRepository {

    private final JdbcTemplate jdbcTemplate;

    // SELECT query statement - Adjust according to actual business requirements
    // This assumes querying and reconciling data from MIRROR_AR and MIRROR_AR_DETAIL tables
    private static final String SELECT_QUERY = """
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
        WHERE MA.BUSINESS_DATE = ?
        GROUP BY MA.BE_ID, MA.BUSINESS_DATE, MAD.AR_ID, MAD.ID_TYPE
        """;

    // INSERT statement - Insert into UD AR reconciliation result table
    // Adjust columns according to actual table structure
    private static final String INSERT_QUERY = """
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
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'SUCCESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        """;

    @Override
    @Transactional
    public boolean callReconciliationProcedure(LocalDate settlementDate) {
        try {
            log.info("UdArReconciliationRepositoryImpl: Starting SELECT query and insert data, settlement date: {}", settlementDate);
            
            // Execute SELECT query
            List<UdArReconciliationRow> rows = jdbcTemplate.query(
                    SELECT_QUERY,
                    (rs, rowNum) -> mapRow(rs, rowNum),
                    Date.valueOf(settlementDate)
            );
            
            log.info("UdArReconciliationRepositoryImpl: Found {} records", rows.size());
            
            if (rows.isEmpty()) {
                log.warn("UdArReconciliationRepositoryImpl: No data found, settlement date: {}", settlementDate);
                return true; // No data is considered success
            }
            
            // Batch insert data
            int[] updateCounts = jdbcTemplate.batchUpdate(
                    INSERT_QUERY,
                    new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            UdArReconciliationRow row = rows.get(i);
                            ps.setInt(1, row.getBeId());
                            ps.setDate(2, Date.valueOf(settlementDate));
                            ps.setString(3, row.getArTypeIdentifier());
                            ps.setString(4, row.getCardMediaTypeId());
                            ps.setInt(5, row.getTransactionCount());
                            ps.setLong(6, row.getTotalCount());
                            ps.setBigDecimal(7, row.getTotalValue());
                            ps.setInt(8, row.getDeviceCount());
                        }

                        @Override
                        public int getBatchSize() {
                            return rows.size();
                        }
                    }
            );
            
            int totalInserted = 0;
            for (int count : updateCounts) {
                totalInserted += count;
            }
            
            log.info("UdArReconciliationRepositoryImpl: Successfully inserted {} records into UD_AR_RECONCILIATION_RESULT, settlement date: {}", totalInserted, settlementDate);
            
            return totalInserted > 0;
            
        } catch (Exception e) {
            log.error("UdArReconciliationRepositoryImpl: Exception occurred while executing SELECT query and insert data, settlement date: {}", settlementDate, e);
            throw new RuntimeException("Failed to execute UD AR reconciliation: " + e.getMessage(), e);
        }
    }

    /**
     * Map query results to object
     */
    private UdArReconciliationRow mapRow(ResultSet rs, int rowNum) throws SQLException {
        return UdArReconciliationRow.builder()
                .beId(rs.getInt("BE_ID"))
                .arTypeIdentifier(rs.getString("AR_TYPE_IDENTIFIER"))
                .cardMediaTypeId(rs.getString("CARD_MEDIA_TYPE_ID"))
                .transactionCount(rs.getInt("TRANSACTION_COUNT"))
                .totalCount(rs.getLong("TOTAL_COUNT"))
                .totalValue(rs.getBigDecimal("TOTAL_VALUE"))
                .deviceCount(rs.getInt("DEVICE_COUNT"))
                .build();
    }

    /**
     * Inner class: used to store query results
     */
    @lombok.Data
    @lombok.Builder
    private static class UdArReconciliationRow {
        private Integer beId;
        private String arTypeIdentifier;
        private String cardMediaTypeId;
        private Integer transactionCount;
        private Long totalCount;
        private BigDecimal totalValue;
        private Integer deviceCount;
    }
}

