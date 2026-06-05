package com.jzyz.gtreviewassistant.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProblemGtDecision {
    private Long id;
    private Long projectId;
    private String candidateKey;
    private String candidateId;
    private String decision;
    private String issueType;
    private String comment;
    private String reviewer;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
