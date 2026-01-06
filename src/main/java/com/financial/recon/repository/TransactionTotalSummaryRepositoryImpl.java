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
public class TransactionTotalSummaryRepositoryImpl implements TransactionTotalSummaryRepository {

    private final JdbcTemplate jdbcTemplate;

    // SELECT query statement - Query and aggregate data from MIRROR_RAW_TXN table
    // Note: This query assumes MIRROR_RAW_TXN table contains SETTLEMENT_STATUS and TXN_AMOUNT columns
    // If the actual table structure is different, please adjust this query according to actual column names
    private static final String SELECT_QUERY = """
        SELECT 
            SETTLEMENT_DATE,
            TXN_TYPE,
            TXN_SUBTYPE,
            BE_ID,
            DEBTOR_BE_ID,
            CREDITOR_BE_ID,
            ISSUER_ID,
            DEVICE_ID,
            BE_BUSINESS_DATE,
            PRODUCT_CODE,
            APPORTIONMENT_VALUE,
            SUM(CASE WHEN SETTLEMENT_STATUS = 'SETTLED' THEN 1 ELSE 0 END) AS UD_SETTLE_COUNT,
            SUM(CASE WHEN SETTLEMENT_STATUS = 'SETTLED' THEN TXN_AMOUNT ELSE 0 END) AS UD_SETTLE_AMOUNT,
            SUM(CASE WHEN SETTLEMENT_STATUS != 'SETTLED' OR SETTLEMENT_STATUS IS NULL THEN 1 ELSE 0 END) AS UD_NOT_SETTLE_COUNT,
            SUM(CASE WHEN SETTLEMENT_STATUS != 'SETTLED' OR SETTLEMENT_STATUS IS NULL THEN TXN_AMOUNT ELSE 0 END) AS UD_NOT_SETTLE_AMOUNT
        FROM MIRROR_RAW_TXN
        WHERE SETTLEMENT_DATE = ?
        GROUP BY DEVICE_ID, TXN_TYPE, TXN_SUBTYPE, BE_BUSINESS_DATE, SETTLEMENT_DATE,
                 BE_ID, DEBTOR_BE_ID, CREDITOR_BE_ID, ISSUER_ID, PRODUCT_CODE, APPORTIONMENT_VALUE
        """;

    // INSERT statement - Insert into TRANSACTION_TOTAL table
    private static final String INSERT_QUERY = """
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
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    @Override
    @Transactional
    public boolean callCalculateTransactionTotalProcedure(LocalDate settlementDate) {
        try {
            log.info("TransactionTotalSummaryRepositoryImpl: Starting SELECT query and insert data, settlement date: {}", settlementDate);
            
            // Execute SELECT query
            List<TransactionTotalSummaryRow> rows = jdbcTemplate.query(
                    SELECT_QUERY,
                    (rs, rowNum) -> mapRow(rs, rowNum),
                    Date.valueOf(settlementDate)
            );
            
            log.info("TransactionTotalSummaryRepositoryImpl: Found {} records", rows.size());
            
            if (rows.isEmpty()) {
                log.warn("TransactionTotalSummaryRepositoryImpl: No data found, settlement date: {}", settlementDate);
                return true; // No data is considered success
            }
            
            // Batch insert data
            int[] updateCounts = jdbcTemplate.batchUpdate(
                    INSERT_QUERY,
                    new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            TransactionTotalSummaryRow row = rows.get(i);
                            ps.setDate(1, row.getSettlementDate() != null ? Date.valueOf(row.getSettlementDate()) : null);
                            ps.setString(2, row.getTxnType());
                            ps.setString(3, row.getTxnSubtype());
                            if (row.getBeId() != null) {
                                ps.setInt(4, row.getBeId());
                            } else {
                                ps.setNull(4, java.sql.Types.INTEGER);
                            }
                            if (row.getDebtorBeId() != null) {
                                ps.setInt(5, row.getDebtorBeId());
                            } else {
                                ps.setNull(5, java.sql.Types.INTEGER);
                            }
                            if (row.getCreditorBeId() != null) {
                                ps.setInt(6, row.getCreditorBeId());
                            } else {
                                ps.setNull(6, java.sql.Types.INTEGER);
                            }
                            ps.setString(7, row.getIssuerId());
                            ps.setString(8, row.getDeviceId());
                            ps.setLong(9, row.getUdSettleCount() != null ? row.getUdSettleCount() : 0L);
                            ps.setBigDecimal(10, row.getUdSettleAmount());
                            ps.setLong(11, row.getUdNotSettleCount() != null ? row.getUdNotSettleCount() : 0L);
                            ps.setBigDecimal(12, row.getUdNotSettleAmount());
                            ps.setDate(13, row.getBeBusinessDate() != null ? Date.valueOf(row.getBeBusinessDate()) : null);
                            ps.setString(14, row.getProductCode());
                            ps.setBigDecimal(15, row.getApportionmentValue());
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
            
            log.info("TransactionTotalSummaryRepositoryImpl: Successfully inserted {} records into TRANSACTION_TOTAL, settlement date: {}", totalInserted, settlementDate);
            
            return totalInserted > 0;
            
        } catch (Exception e) {
            log.error("TransactionTotalSummaryRepositoryImpl: Exception occurred while executing SELECT query and insert data, settlement date: {}", settlementDate, e);
            throw new RuntimeException("Failed to calculate transaction total: " + e.getMessage(), e);
        }
    }

    /**
     * Map query results to object
     */
    private TransactionTotalSummaryRow mapRow(ResultSet rs, int rowNum) throws SQLException {
        java.sql.Date settlementDate = rs.getDate("SETTLEMENT_DATE");
        java.sql.Date beBusinessDate = rs.getDate("BE_BUSINESS_DATE");
        
        return TransactionTotalSummaryRow.builder()
                .settlementDate(settlementDate != null ? settlementDate.toLocalDate() : null)
                .txnType(rs.getString("TXN_TYPE"))
                .txnSubtype(rs.getString("TXN_SUBTYPE"))
                .beId(getIntOrNull(rs, "BE_ID"))
                .debtorBeId(getIntOrNull(rs, "DEBTOR_BE_ID"))
                .creditorBeId(getIntOrNull(rs, "CREDITOR_BE_ID"))
                .issuerId(rs.getString("ISSUER_ID"))
                .deviceId(rs.getString("DEVICE_ID"))
                .udSettleCount(rs.getLong("UD_SETTLE_COUNT"))
                .udSettleAmount(rs.getBigDecimal("UD_SETTLE_AMOUNT"))
                .udNotSettleCount(rs.getLong("UD_NOT_SETTLE_COUNT"))
                .udNotSettleAmount(rs.getBigDecimal("UD_NOT_SETTLE_AMOUNT"))
                .beBusinessDate(beBusinessDate != null ? beBusinessDate.toLocalDate() : null)
                .productCode(rs.getString("PRODUCT_CODE"))
                .apportionmentValue(rs.getBigDecimal("APPORTIONMENT_VALUE"))
                .build();
    }

    /**
     * Safely get Integer value, handle NULL case
     */
    private Integer getIntOrNull(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    /**
     * Inner class: used to store query results
     */
    @lombok.Data
    @lombok.Builder
    private static class TransactionTotalSummaryRow {
        private LocalDate settlementDate;
        private String txnType;
        private String txnSubtype;
        private Integer beId;
        private Integer debtorBeId;
        private Integer creditorBeId;
        private String issuerId;
        private String deviceId;
        private Long udSettleCount;
        private BigDecimal udSettleAmount;
        private Long udNotSettleCount;
        private BigDecimal udNotSettleAmount;
        private LocalDate beBusinessDate;
        private String productCode;
        private BigDecimal apportionmentValue;
    }
}

