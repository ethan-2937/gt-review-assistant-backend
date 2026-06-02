package com.jzyz.gtreviewassistant.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StructureRow {
    private Long id;
    private Long tableId;
    private Long projectId;
    private String side;
    private String noteNo;
    private String tableTitle;
    private String rowKey;
    private String rowPath;
    private String rowLeaf;
    private String itemKey;
    private Integer rowOrder;
    private String sourceLocator;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
