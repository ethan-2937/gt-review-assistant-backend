package com.jzyz.gtreviewassistant.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ProblemGtBatchDecisionRequest {
    @NotEmpty
    private List<String> candidateKeys;

    @NotBlank
    private String decision;

    private String issueType;
    private String comment;
    private String reviewer;
}
