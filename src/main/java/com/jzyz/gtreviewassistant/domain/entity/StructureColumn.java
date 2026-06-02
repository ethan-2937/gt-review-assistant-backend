package com.jzyz.gtreviewassistant.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StructureColumn {
    private Long id;
    private Long tableId;
    private Long projectId;
    private String side;
    private String noteNo;
    private String tableTitle;
    private String columnKey;
    private String columnPath;
    private String columnLeaf;
    private String itemKey;
    private Integer columnOrder;
    private String sourceLocator;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
