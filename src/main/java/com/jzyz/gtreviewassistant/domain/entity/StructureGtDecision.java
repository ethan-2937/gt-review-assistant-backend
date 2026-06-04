package com.jzyz.gtreviewassistant.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StructureGtDecision {
    private Long id;
    private Long projectId;
    private String decisionKey;
    private String viewMode;
    private Long runId;
    private String noteNo;
    private String level;
    private String itemKey;
    private String runtimeSide;
    private String decision;
    private String aliasText;
    private String mergeTargetKey;
    private String comment;
    private String reviewer;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
