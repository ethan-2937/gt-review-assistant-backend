package com.jzyz.gtreviewassistant.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProjectCreateRequest {
    @NotBlank(message = "项目 key 不能为空")
    private String projectKey;

    @NotBlank(message = "项目名称不能为空")
    private String name;

    private String yearLabel;
    private String datasetKey;
    private String versionLabel;
    private String createdBy;
}