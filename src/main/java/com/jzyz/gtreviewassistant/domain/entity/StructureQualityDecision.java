package com.jzyz.gtreviewassistant.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StructureQualityDecision {
    private Long id;
    private Long projectId;
    private String issueKey;
    private String issueType;
    private String severity;
    private String side;
    private String noteNo;
    private Long tableId;
    private String tableTitle;
    private String level;
    private Long refId;
    private String decision;
    private String comment;
    private String reviewer;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
