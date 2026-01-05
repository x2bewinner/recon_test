package com.financial.recon.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "MIRROR_AR_DETAIL_EX")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MirrorArDetailEx {
    @Id
    @Column(name = "REFERENCE_ID", length = 64)
    private String referenceId;

    @Column(name = "REF_RECORD_ID", length = 5)
    private String refRecordId;

    @Column(name = "AR_ENTRY_ID", length = 5)
    private String arEntryId;

    @Column(name = "AR_ID", length = 20)
    private String arId;

    @Column(name = "ID_TYPE", length = 5)
    private String idType;

    @Column(name = "COUNT", precision = 19, scale = 0)
    private Long count;

    @Column(name = "VALUE", precision = 19, scale = 0)
    private BigDecimal value;

    @Column(name = "LAST_UPDATED_TIME")
    private OffsetDateTime lastUpdatedTime;
}

