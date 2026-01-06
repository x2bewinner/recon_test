package com.xxcards.xbtx.udar.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "MIRROR_AR_DETAIL")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(MirrorArDetailId.class)
public class MirrorArDetail {
    @Id
    @Column(name = "REFERENCE_ID", length = 64, nullable = false)
    private String referenceId;

    @Id
    @Column(name = "REF_RECORD_ID", length = 5, nullable = false)
    private String refRecordId;

    @Id
    @Column(name = "AR_ENTRY_ID", length = 5, nullable = false)
    private String arEntryId;

    @Column(name = "AR_ID", length = 20, nullable = false)
    private String arId;

    @Column(name = "ID_TYPE", precision = 10, scale = 0, nullable = false)
    private Integer idType;

    @Column(name = "COUNT", precision = 19, scale = 0)
    private Long count;

    @Column(name = "VALUE", precision = 19, scale = 0)
    private BigDecimal value;

    @Column(name = "LAST_UPDATED_TIME")
    private OffsetDateTime lastUpdatedTime;
}

