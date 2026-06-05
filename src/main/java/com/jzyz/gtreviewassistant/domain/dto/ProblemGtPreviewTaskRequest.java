package com.jzyz.gtreviewassistant.domain.dto;

import lombok.Data;

@Data
public class ProblemGtPreviewTaskRequest {
    private String sourceRunKey;
    private String sourceRoot;
    private String outputDir;
    private Integer limit;
}
