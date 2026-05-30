package com.jzyz.gtreviewassistant.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StructureNote {
    private Long id;
    private Long projectId;
    private String side;
    private String noteNo;
    private String noteName;
    private String sourceFilePath;
    private String sourceLocator;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}