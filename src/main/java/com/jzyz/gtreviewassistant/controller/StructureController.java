package com.jzyz.gtreviewassistant.controller;

import com.jzyz.gtreviewassistant.common.ApiResponse;
import com.jzyz.gtreviewassistant.domain.dto.ImportResult;
import com.jzyz.gtreviewassistant.domain.dto.NoteDetail;
import com.jzyz.gtreviewassistant.domain.dto.NoteSummary;
import com.jzyz.gtreviewassistant.domain.dto.PageResult;
import com.jzyz.gtreviewassistant.domain.dto.StructureCoverageItem;
import com.jzyz.gtreviewassistant.domain.dto.StructureImportRequest;
import com.jzyz.gtreviewassistant.domain.dto.StructureOverview;
import com.jzyz.gtreviewassistant.domain.entity.StructureDiff;
import com.jzyz.gtreviewassistant.service.StructureCompareService;
import com.jzyz.gtreviewassistant.service.StructureImportService;
import com.jzyz.gtreviewassistant.service.StructureQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/structures")
public class StructureController {
    private final StructureImportService importService;
    private final StructureCompareService compareService;
    private final StructureQueryService queryService;

    @PostMapping("/import")
    public ApiResponse<ImportResult> importStructure(@PathVariable Long projectId,
                                                     @Valid @RequestBody StructureImportRequest request) {
        return ApiResponse.ok("结构台账已导入", importService.importStructure(projectId, request));
    }

    @PostMapping("/compare")
    public ApiResponse<Map<String, Integer>> rebuildDiffs(@PathVariable Long projectId) {
        int diffCount = compareService.rebuildDiffs(projectId);
        return ApiResponse.ok("PDF vs Excel 结构差异已重新生成", Map.of("diffCount", diffCount));
    }

    @GetMapping("/overview")
    public ApiResponse<StructureOverview> overview(@PathVariable Long projectId) {
        return ApiResponse.ok(queryService.overview(projectId));
    }

    @GetMapping("/notes")
    public ApiResponse<List<NoteSummary>> notes(@PathVariable Long projectId) {
        return ApiResponse.ok(queryService.notes(projectId));
    }

    @GetMapping("/notes/{noteNo}")
    public ApiResponse<NoteDetail> noteDetail(@PathVariable Long projectId, @PathVariable String noteNo) {
        return ApiResponse.ok(queryService.noteDetail(projectId, noteNo));
    }

    @GetMapping("/notes/{noteNo}/coverage")
    public ApiResponse<PageResult<StructureCoverageItem>> coverageItems(@PathVariable Long projectId,
                                                                        @PathVariable String noteNo,
                                                                        @RequestParam(defaultValue = "cell") String level,
                                                                        @RequestParam(required = false) String keyword,
                                                                        @RequestParam(defaultValue = "1") int pageNum,
                                                                        @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(queryService.coverageItems(projectId, noteNo, level, keyword, pageNum, pageSize));
    }

    @GetMapping("/diffs")
    public ApiResponse<List<StructureDiff>> diffs(@PathVariable Long projectId,
                                                  @RequestParam(required = false) String noteNo,
                                                  @RequestParam(required = false) String diffLevel) {
        return ApiResponse.ok(queryService.diffs(projectId, noteNo, diffLevel));
    }
}
