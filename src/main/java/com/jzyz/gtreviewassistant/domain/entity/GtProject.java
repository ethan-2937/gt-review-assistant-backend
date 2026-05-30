package com.jzyz.gtreviewassistant.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GtProject {
    private Long id;
    private String projectKey;
    private String name;
    private String yearLabel;
    private String datasetKey;
    private String versionLabel;
    private String status;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}