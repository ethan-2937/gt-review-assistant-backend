package com.jzyz.gtreviewassistant.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RuntimeRunImportRequest {
    @NotBlank
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

    @Valid
    private List<ItemPayload> items = new ArrayList<>();

    @Data
    public static class ItemPayload {
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
    }
}
