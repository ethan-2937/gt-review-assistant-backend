package com.jzyz.gtreviewassistant.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProblemGtCandidate {
    private Long id;
    private Long projectId;
    private String sourceRunKey;
    private String candidateId;
    private String candidateKey;
    private String noteNo;
    private String noteName;
    private String folder;
    private String source;
    private String bucket;
    private String risk;
    private String recommendedAction;
    private String suggestedIssueType;
    private String reason;
    private String candidateKind;
    private Integer priority;
    private String periodBucket;
    private String tablePath;
    private String sectionPath;
    private String rowLabel;
    private String columnLabel;
    private String periodLabel;
    private String sourceValue;
    private String targetValue;
    private String missingCells;
    private String missingValues;
    private String currentGtOverlap;
    private String currentFinalHint;
    private String manualDecision;
    private String manualRationale;
    private String sourceLocator;
    private String targetLocator;
    private String pdfContext;
    private String rawPayloadJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
