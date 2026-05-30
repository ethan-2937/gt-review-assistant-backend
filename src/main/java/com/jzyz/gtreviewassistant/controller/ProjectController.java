package com.jzyz.gtreviewassistant.controller;

import com.jzyz.gtreviewassistant.common.ApiResponse;
import com.jzyz.gtreviewassistant.domain.dto.ProjectCreateRequest;
import com.jzyz.gtreviewassistant.domain.entity.GtProject;
import com.jzyz.gtreviewassistant.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping
    public ApiResponse<GtProject> create(@Valid @RequestBody ProjectCreateRequest request) {
        return ApiResponse.ok("项目已创建", projectService.create(request));
    }

    @GetMapping
    public ApiResponse<List<GtProject>> list() {
        return ApiResponse.ok(projectService.list());
    }

    @GetMapping("/{projectId}")
    public ApiResponse<GtProject> detail(@PathVariable Long projectId) {
        return ApiResponse.ok(projectService.getRequired(projectId));
    }
}