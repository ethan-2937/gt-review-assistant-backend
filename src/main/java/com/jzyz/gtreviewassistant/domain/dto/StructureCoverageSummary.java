package com.jzyz.gtreviewassistant.domain.dto;

import lombok.Data;

@Data
public class StructureCoverageSummary {
    private Long projectId;
    private String noteNo;
    private String level;

    private Integer sourceTotalCount;
    private Integer pdfCount;
    private Integer excelCount;
    private Integer bothSideCount;
    private Integer pdfOnlyCount;
    private Integer excelOnlyCount;

    private Integer runtimeMatchedCount;
    private Integer runtimeMissingCount;
    private Integer runtimeOnlyCount;
}
