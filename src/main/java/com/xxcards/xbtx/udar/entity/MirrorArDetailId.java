package com.xxcards.xbtx.udar.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MirrorArDetailId implements Serializable {
    private String referenceId;
    private String refRecordId;
    private String arEntryId;
}

