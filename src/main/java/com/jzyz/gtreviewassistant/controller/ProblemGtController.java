package com.jzyz.gtreviewassistant.controller;

import com.jzyz.gtreviewassistant.common.ApiResponse;
import com.jzyz.gtreviewassistant.domain.dto.PageResult;
import com.jzyz.gtreviewassistant.domain.dto.ProblemGtBatchDecisionRequest;
import com.jzyz.gtreviewassistant.domain.dto.ProblemGtBatchDecisionResult;
import com.jzyz.gtreviewassistant.domain.dto.ProblemGtDecisionRequest;
import com.jzyz.gtreviewassistant.domain.dto.ProblemGtImportRequest;
import com.jzyz.gtreviewassistant.domain.dto.ProblemGtImportResult;
import com.jzyz.gtreviewassistant.domain.dto.ProblemGtOverview;
import com.jzyz.gtreviewassistant.domain.dto.ProblemGtPreviewTaskRequest;
import com.jzyz.gtreviewassistant.domain.dto.ProblemGtPreviewTaskStatus;
import com.jzyz.gtreviewassistant.domain.dto.ProblemGtReviewItem;
import com.jzyz.gtreviewassistant.domain.entity.ProblemGtDecision;
import com.jzyz.gtreviewassistant.service.ProblemGtPreviewTaskService;
import com.jzyz.gtreviewassistant.service.ProblemGtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/problem-gt")
public class ProblemGtController {
    private final ProblemGtService problemGtService;
    private final ProblemGtPreviewTaskService previewTaskService;

    @PostMapping("/import")
    public ApiResponse<ProblemGtImportResult> importCandidates(@PathVariable Long projectId,
                                                               @Valid @RequestBody ProblemGtImportRequest request) {
        return ApiResponse.ok("问题GT候选已导入", problemGtService.importCandidates(projectId, request));
    }

    @GetMapping("/overview")
    public ApiResponse<ProblemGtOverview> overview(@PathVariable Long projectId) {
        return ApiResponse.ok(problemGtService.overview(projectId));
    }

    @GetMapping("/candidates")
    public ApiResponse<PageResult<ProblemGtReviewItem>> candidates(@PathVariable Long projectId,
                                                                   @RequestParam(required = false) String sourceRunKey,
                                                                   @RequestParam(required = false) String bucket,
                                                                   @RequestParam(required = false) String risk,
                                                                   @RequestParam(required = false) String noteNo,
                                                                   @RequestParam(required = false) String decision,
                                                                   @RequestParam(required = false) String keyword,
                                                                   @RequestParam(defaultValue = "1") int pageNum,
                                                                   @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok(problemGtService.reviewItems(projectId, sourceRunKey, bucket, risk, noteNo, decision, keyword, pageNum, pageSize));
    }

    @PostMapping("/decisions")
    public ApiResponse<ProblemGtDecision> saveDecision(@PathVariable Long projectId,
                                                       @Valid @RequestBody ProblemGtDecisionRequest request) {
        return ApiResponse.ok("问题GT裁决已保存", problemGtService.saveDecision(projectId, request));
    }

    @PostMapping("/decisions/batch")
    public ApiResponse<ProblemGtBatchDecisionResult> saveBatchDecision(@PathVariable Long projectId,
                                                                       @Valid @RequestBody ProblemGtBatchDecisionRequest request) {
        return ApiResponse.ok("问题GT批量裁决已保存", problemGtService.saveBatchDecision(projectId, request));
    }

    @PostMapping("/previews/generate")
    public ApiResponse<ProblemGtPreviewTaskStatus> generatePreviews(@PathVariable Long projectId,
                                                                    @RequestBody(required = false) ProblemGtPreviewTaskRequest request) {
        return ApiResponse.ok("问题GT截图生成任务已启动", previewTaskService.start(projectId, request == null ? new ProblemGtPreviewTaskRequest() : request));
    }

    @GetMapping("/previews/tasks/{taskId}")
    public ApiResponse<ProblemGtPreviewTaskStatus> previewTask(@PathVariable Long projectId,
                                                               @PathVariable String taskId) {
        return ApiResponse.ok(previewTaskService.get(taskId));
    }

    @GetMapping("/export.json")
    public ResponseEntity<byte[]> exportJson(@PathVariable Long projectId,
                                             @RequestParam(required = false) String sourceRunKey) {
        return download(
                problemGtService.exportProposalJson(projectId, sourceRunKey),
                problemGtService.exportFilename(projectId, sourceRunKey, ".json"),
                MediaType.APPLICATION_JSON
        );
    }

    @GetMapping("/export.md")
    public ResponseEntity<byte[]> exportMarkdown(@PathVariable Long projectId,
                                                 @RequestParam(required = false) String sourceRunKey) {
        return download(
                problemGtService.exportProposalMarkdown(projectId, sourceRunKey),
                problemGtService.exportFilename(projectId, sourceRunKey, ".md"),
                MediaType.valueOf("text/markdown; charset=UTF-8")
        );
    }

    @GetMapping("/export.xlsx")
    public ResponseEntity<byte[]> exportXlsx(@PathVariable Long projectId,
                                             @RequestParam(required = false) String sourceRunKey) {
        return download(
                problemGtService.exportProposalXlsx(projectId, sourceRunKey),
                problemGtService.exportFilename(projectId, sourceRunKey, ".xlsx"),
                MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        );
    }

    private ResponseEntity<byte[]> download(byte[] body, String filename, MediaType mediaType) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .body(body);
    }
}
