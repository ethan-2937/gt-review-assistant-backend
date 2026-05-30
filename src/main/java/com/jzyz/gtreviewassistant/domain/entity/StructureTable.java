package com.jzyz.gtreviewassistant.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StructureTable {
    private Long id;
    private Long noteId;
    private Long projectId;
    private String side;
    private String noteNo;
    private String tableTitle;
    private Integer tableOrder;
    private String sourceLocator;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}