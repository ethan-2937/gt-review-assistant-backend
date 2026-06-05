package com.jzyz.gtreviewassistant.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProblemGtDecisionRequest {
    @NotBlank
    private String candidateKey;

    private String candidateId;

    @NotBlank
    private String decision;

    private String issueType;
    private String comment;
    private String reviewer;
}
