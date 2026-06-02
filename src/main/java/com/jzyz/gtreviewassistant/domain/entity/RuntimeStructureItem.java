package com.jzyz.gtreviewassistant.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RuntimeStructureItem {
    private Long id;
    private Long projectId;
    private Long runId;
    private String caseId;
    private String noteNo;
    private String noteName;
    private String level;
    private String runtimeSide;
    private String itemKey;
    private String tableTitle;
    private String tableProfileId;
    private String rowKey;
    private String rowLabel;
    private String rowPath;
    private String columnKey;
    private String columnLabel;
    private String columnPath;
    private String valueText;
    private String normalizedValue;
    private String valueSignature;
    private String valueType;
    private String sourceLocator;
    private String cellCoordinate;
    private String quoteText;
    private String sourceArtifact;
    private String sourceJsonPath;
    private String locatorMethod;
    private String confidenceLevel;
    private String rawPayloadJson;
    private LocalDateTime createdAt;
}
