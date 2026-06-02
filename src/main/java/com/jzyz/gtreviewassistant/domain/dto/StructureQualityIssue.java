package com.jzyz.gtreviewassistant.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StructureQualityIssue {
    private String issueKey;
    private String issueType;
    private String severity;
    private String side;
    private String noteNo;
    private String noteName;
    private Long tableId;
    private String tableTitle;
    private String level;
    private Long refId;
    private String refText;
    private Integer expectedCount;
    private Integer actualCount;
    private String message;
    private String suggestion;
    private String locator;
    private String decision;
    private String decisionComment;
    private String reviewer;
    private LocalDateTime reviewedAt;
    private LocalDateTime decisionUpdatedAt;
}
