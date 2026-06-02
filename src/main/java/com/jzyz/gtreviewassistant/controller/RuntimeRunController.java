package com.jzyz.gtreviewassistant.controller;

import com.jzyz.gtreviewassistant.common.ApiResponse;
import com.jzyz.gtreviewassistant.domain.dto.RuntimeImportResult;
import com.jzyz.gtreviewassistant.domain.dto.RuntimeRunImportRequest;
import com.jzyz.gtreviewassistant.domain.entity.RuntimeRun;
import com.jzyz.gtreviewassistant.service.RuntimeRunService;
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
@RequestMapping("/api/projects/{projectId}/runtime-runs")
public class RuntimeRunController {
    private final RuntimeRunService runtimeRunService;

    @GetMapping
    public ApiResponse<List<RuntimeRun>> listRuns(@PathVariable Long projectId) {
        return ApiResponse.ok(runtimeRunService.listRuns(projectId));
    }

    @PostMapping("/import")
    public ApiResponse<RuntimeImportResult> importRun(@PathVariable Long projectId,
                                                      @Valid @RequestBody RuntimeRunImportRequest request) {
        return ApiResponse.ok("runtime 运行结构已导入", runtimeRunService.importRun(projectId, request));
    }
}
