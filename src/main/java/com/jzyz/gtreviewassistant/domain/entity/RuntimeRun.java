package com.jzyz.gtreviewassistant.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RuntimeRun {
    private Long id;
    private Long projectId;
    private String runKey;
    private String runRoot;
    private String runType;
    private String runStatus;
    private String datasetKey;
    private String versionLabel;
    private Integer caseCount;
    private String artifactCompleteness;
    private String confidenceLevel;
    private String sourceHost;
    private Integer sourceSampleCount;
    private Integer targetSampleCount;
    private Integer sourceStructuredCellCount;
    private Integer targetStructuredCellCount;
    private Integer tableCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
