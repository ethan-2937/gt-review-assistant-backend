package com.jzyz.gtreviewassistant.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StructureCell {
    private Long id;
    private Long tableId;
    private Long rowId;
    private Long columnId;
    private Long projectId;
    private String side;
    private String noteNo;
    private String tableTitle;
    private String rowKey;
    private String columnKey;
    private String rowPath;
    private String columnPath;
    private String itemKey;
    private String valueText;
    private String normalizedValue;
    private String sourceLocator;
    private String screenshotPath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
