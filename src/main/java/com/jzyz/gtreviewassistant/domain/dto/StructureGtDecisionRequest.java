package com.jzyz.gtreviewassistant.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StructureGtDecisionRequest {
    @NotBlank
    private String decisionKey;

    @NotBlank
    private String viewMode;

    private Long runId;
    private String noteNo;
    private String level;
    private String itemKey;
    private String runtimeSide;

    @NotBlank
    private String decision;

    private String aliasText;
    private String mergeTargetKey;
    private String comment;
    private String reviewer;
}
