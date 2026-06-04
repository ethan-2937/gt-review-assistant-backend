package com.jzyz.gtreviewassistant.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StructureCoverageItem {
    private String level;
    private String itemKey;
    private String noteNo;

    private String pdfTitle;
    private String pdfDetail;
    private String pdfLocator;
    private String pdfPreviewUrl;

    private String excelTitle;
    private String excelDetail;
    private String excelLocator;
    private String excelPreviewUrl;

    private Boolean runtimeRecognized;
    private String runtimeStatus;
    private Integer runtimeMatchCount;
    private String runtimeSide;
    private String runtimeTitle;
    private String runtimeDetail;
    private String runtimeLocator;
    private String runtimeValueText;
    private String runtimeConfidenceLevel;
    private String runtimeSourceArtifact;
    private String runtimeLocatorMethod;

    private String decisionKey;
    private String viewMode;
    private String decision;
    private String decisionComment;
    private String aliasText;
    private String mergeTargetKey;
    private String reviewer;
    private LocalDateTime reviewedAt;
    private LocalDateTime decisionUpdatedAt;
}
