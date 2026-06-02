package com.jzyz.gtreviewassistant.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StructureQualityDecisionRequest {
    @NotBlank(message = "issueKey 不能为空")
    private String issueKey;
    private String issueType;
    private String severity;
    private String side;
    private String noteNo;
    private Long tableId;
    private String tableTitle;
    private String level;
    private Long refId;
    @NotBlank(message = "decision 不能为空")
    private String decision;
    private String comment;
    private String reviewer;
}
