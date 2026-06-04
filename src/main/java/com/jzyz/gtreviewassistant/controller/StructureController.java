package com.jzyz.gtreviewassistant.controller;

import com.jzyz.gtreviewassistant.common.ApiResponse;
import com.jzyz.gtreviewassistant.domain.dto.ImportResult;
import com.jzyz.gtreviewassistant.domain.dto.NoteDetail;
import com.jzyz.gtreviewassistant.domain.dto.NoteSummary;
import com.jzyz.gtreviewassistant.domain.dto.PageResult;
import com.jzyz.gtreviewassistant.domain.dto.StructureCoverageItem;
import com.jzyz.gtreviewassistant.domain.dto.StructureCoverageSummary;
import com.jzyz.gtreviewassistant.domain.dto.StructureGtDecisionRequest;
import com.jzyz.gtreviewassistant.domain.dto.StructureQualityDecisionRequest;
import com.jzyz.gtreviewassistant.domain.dto.StructureImportRequest;
import com.jzyz.gtreviewassistant.domain.dto.StructureOverview;
import com.jzyz.gtreviewassistant.domain.dto.StructureQualityIssue;
import com.jzyz.gtreviewassistant.domain.entity.StructureDiff;
import com.jzyz.gtreviewassistant.domain.entity.StructureGtDecision;
import com.jzyz.gtreviewassistant.domain.entity.StructureQualityDecision;
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
        return ApiResponse.ok("结构GT台账已导入", importService.importStructure(projectId, request));
    }

    @PostMapping("/import-notes")
    public ApiResponse<ImportResult> importStructureNotes(@PathVariable Long projectId,
                                                          @Valid @RequestBody StructureImportRequest request) {
        return ApiResponse.ok("Structure note imported safely", importService.importStructureNotes(projectId, request));
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
                                                                        @RequestParam(defaultValue = "row") String level,
                                                                        @RequestParam(required = false) String keyword,
                                                                        @RequestParam(required = false) Long runId,
                                                                        @RequestParam(required = false) String matchStatus,
                                                                        @RequestParam(defaultValue = "1") int pageNum,
                                                                        @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(queryService.coverageItems(projectId, noteNo, level, keyword, runId, matchStatus, pageNum, pageSize));
    }

    @GetMapping("/notes/{noteNo}/coverage/summary")
    public ApiResponse<StructureCoverageSummary> coverageSummary(@PathVariable Long projectId,
                                                                 @PathVariable String noteNo,
                                                                 @RequestParam(defaultValue = "row") String level,
                                                                 @RequestParam(required = false) Long runId) {
        return ApiResponse.ok(queryService.coverageSummary(projectId, noteNo, level, runId));
    }

    @GetMapping("/notes/{noteNo}/coverage/runtime-only")
    public ApiResponse<PageResult<StructureCoverageItem>> runtimeOnlyItems(@PathVariable Long projectId,
                                                                           @PathVariable String noteNo,
                                                                           @RequestParam(defaultValue = "row") String level,
                                                                           @RequestParam(required = false) String keyword,
                                                                           @RequestParam(required = false) Long runId,
                                                                           @RequestParam(defaultValue = "1") int pageNum,
                                                                           @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(queryService.runtimeOnlyItems(projectId, noteNo, level, keyword, runId, pageNum, pageSize));
    }

    @PostMapping("/gt-decisions")
    public ApiResponse<StructureGtDecision> saveGtDecision(@PathVariable Long projectId,
                                                           @Valid @RequestBody StructureGtDecisionRequest request) {
        return ApiResponse.ok("结构GT裁决已保存", queryService.saveGtDecision(projectId, request));
    }

    @GetMapping("/quality/issues")
    public ApiResponse<PageResult<StructureQualityIssue>> qualityIssues(@PathVariable Long projectId,
                                                                        @RequestParam(required = false) String noteNo,
                                                                        @RequestParam(required = false) String severity,
                                                                        @RequestParam(required = false) String issueType,
                                                                        @RequestParam(required = false) String side,
                                                                        @RequestParam(required = false) String decision,
                                                                        @RequestParam(defaultValue = "1") int pageNum,
                                                                        @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(queryService.qualityIssues(projectId, noteNo, severity, issueType, side, decision, pageNum, pageSize));
    }

    @PostMapping("/quality/decisions")
    public ApiResponse<StructureQualityDecision> saveQualityDecision(@PathVariable Long projectId,
                                                                     @Valid @RequestBody StructureQualityDecisionRequest request) {
        return ApiResponse.ok("结构GT 裁决已保存", queryService.saveQualityDecision(projectId, request));
    }

    @GetMapping("/diffs")
    public ApiResponse<List<StructureDiff>> diffs(@PathVariable Long projectId,
                                                  @RequestParam(required = false) String noteNo,
                                                  @RequestParam(required = false) String diffLevel) {
        return ApiResponse.ok(queryService.diffs(projectId, noteNo, diffLevel));
    }
}
