package com.jzyz.gtreviewassistant.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jzyz.gtreviewassistant.common.SimpleXlsxWriter;
import com.jzyz.gtreviewassistant.common.TextKeys;
import com.jzyz.gtreviewassistant.domain.dto.PageResult;
import com.jzyz.gtreviewassistant.domain.dto.ProblemGtDecisionRequest;
import com.jzyz.gtreviewassistant.domain.dto.ProblemGtImportRequest;
import com.jzyz.gtreviewassistant.domain.dto.ProblemGtImportResult;
import com.jzyz.gtreviewassistant.domain.dto.ProblemGtOverview;
import com.jzyz.gtreviewassistant.domain.dto.ProblemGtReviewItem;
import com.jzyz.gtreviewassistant.domain.entity.ProblemGtCandidate;
import com.jzyz.gtreviewassistant.domain.entity.ProblemGtDecision;
import com.jzyz.gtreviewassistant.mapper.ProblemGtCandidateMapper;
import com.jzyz.gtreviewassistant.mapper.ProblemGtDecisionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProblemGtService {
    private final ProjectService projectService;
    private final ProblemGtCandidateMapper candidateMapper;
    private final ProblemGtDecisionMapper decisionMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public ProblemGtImportResult importCandidates(Long projectId, ProblemGtImportRequest request) {
        projectService.getRequired(projectId);
        String sourceRunKey = TextKeys.clean(request.getSourceRunKey());
        LocalDateTime now = LocalDateTime.now();
        candidateMapper.deleteByProjectAndRunKey(projectId, sourceRunKey);

        int lockedCount = 0;
        int highCount = 0;
        int mediumCount = 0;
        int pdfLowCount = 0;
        List<ProblemGtCandidate> batch = new ArrayList<>(200);
        for (ProblemGtImportRequest.CandidatePayload payload : request.getCandidates()) {
            ProblemGtCandidate candidate = toCandidate(projectId, sourceRunKey, payload, now);
            batch.add(candidate);
            if ("locked_confirmed_skip".equals(candidate.getBucket())) lockedCount++;
            if ("high".equals(candidate.getRisk())) highCount++;
            if ("medium".equals(candidate.getRisk())) mediumCount++;
            if ("low_confidence_pdf_extra_value_pool".equals(candidate.getBucket())) pdfLowCount++;
            if (batch.size() >= 200) {
                candidateMapper.insertBatch(batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            candidateMapper.insertBatch(batch);
        }
        return new ProblemGtImportResult(sourceRunKey, request.getCandidates().size(), lockedCount, highCount, mediumCount, pdfLowCount);
    }

    public ProblemGtOverview overview(Long projectId) {
        projectService.getRequired(projectId);
        ProblemGtOverview overview = candidateMapper.selectOverview(projectId);
        if (overview == null) {
            overview = new ProblemGtOverview();
        }
        fillNulls(overview);
        return overview;
    }

    public PageResult<ProblemGtReviewItem> reviewItems(Long projectId,
                                                       String sourceRunKey,
                                                       String bucket,
                                                       String risk,
                                                       String noteNo,
                                                       String decision,
                                                       String keyword,
                                                       int pageNum,
                                                       int pageSize) {
        projectService.getRequired(projectId);
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 5), 100);
        String safeDecision = normalizeDecisionFilter(decision);
        PageHelper.startPage(safePageNum, safePageSize);
        List<ProblemGtReviewItem> items = candidateMapper.selectReviewItems(
                projectId,
                TextKeys.clean(sourceRunKey),
                TextKeys.clean(bucket),
                TextKeys.clean(risk),
                TextKeys.clean(noteNo),
                safeDecision,
                TextKeys.clean(keyword),
                TextKeys.searchKey(keyword)
        );
        PageInfo<ProblemGtReviewItem> pageInfo = new PageInfo<>(items);
        return new PageResult<>(
                pageInfo.getPageNum(),
                pageInfo.getPageSize(),
                pageInfo.getTotal(),
                pageInfo.getPages(),
                pageInfo.getList()
        );
    }

    @Transactional
    public ProblemGtDecision saveDecision(Long projectId, ProblemGtDecisionRequest request) {
        projectService.getRequired(projectId);
        LocalDateTime now = LocalDateTime.now();
        ProblemGtDecision decision = new ProblemGtDecision();
        decision.setProjectId(projectId);
        decision.setCandidateKey(TextKeys.clean(request.getCandidateKey()));
        decision.setCandidateId(TextKeys.clean(request.getCandidateId()));
        decision.setDecision(normalizeDecision(request.getDecision()));
        decision.setIssueType(TextKeys.clean(request.getIssueType()));
        decision.setComment(TextKeys.clean(request.getComment()));
        decision.setReviewer(TextKeys.firstNonBlank(request.getReviewer(), "local-user"));
        decision.setReviewedAt(now);
        decision.setCreatedAt(now);
        decision.setUpdatedAt(now);
        decisionMapper.upsert(decision);
        return decisionMapper.selectByProjectAndCandidateKey(projectId, decision.getCandidateKey());
    }

    public byte[] exportProposalJson(Long projectId, String sourceRunKey) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsBytes(buildExportPackage(projectId, sourceRunKey));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("failed to export problem GT proposal json", ex);
        }
    }

    public byte[] exportProposalMarkdown(Long projectId, String sourceRunKey) {
        ExportData data = loadExportData(projectId, sourceRunKey);
        StringBuilder md = new StringBuilder();
        md.append("# 问题GT裁决 Proposal\n\n");
        md.append("- 状态：proposal_only_not_applied\n");
        md.append("- run：`").append(data.sourceRunKey()).append("`\n");
        md.append("- 说明：本文件只汇总页面中的人工裁决，不会修改 final_gt workbook。\n\n");
        md.append("## 数量\n\n");
        for (Map.Entry<String, Object> entry : data.summary().entrySet()) {
            md.append("- ").append(entry.getKey()).append("：").append(entry.getValue()).append("\n");
        }
        md.append("\n## 建议补入\n\n");
        appendMarkdownRows(md, data.adds());
        md.append("\n## 建议排除/重复/表级合并\n\n");
        appendMarkdownRows(md, data.excluded());
        md.append("\n## 未完成\n\n");
        appendMarkdownRows(md, data.unresolved());
        return md.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportProposalXlsx(Long projectId, String sourceRunKey) {
        ExportData data = loadExportData(projectId, sourceRunKey);
        List<String> summaryHeaders = List.of("metric", "value");
        List<List<String>> summaryRows = data.summary().entrySet().stream()
                .map(entry -> List.of(entry.getKey(), String.valueOf(entry.getValue())))
                .toList();
        return SimpleXlsxWriter.write(List.of(
                new SimpleXlsxWriter.Sheet("摘要", summaryHeaders, summaryRows),
                new SimpleXlsxWriter.Sheet("建议补入", exportHeaders(), toSheetRows(data.adds())),
                new SimpleXlsxWriter.Sheet("排除重复", exportHeaders(), toSheetRows(data.excluded())),
                new SimpleXlsxWriter.Sheet("未完成", exportHeaders(), toSheetRows(data.unresolved())),
                new SimpleXlsxWriter.Sheet("全部候选", exportHeaders(), toSheetRows(data.allRows()))
        ));
    }

    public String exportFilename(Long projectId, String sourceRunKey, String suffix) {
        String runKey = resolveSourceRunKey(projectId, sourceRunKey);
        return "problem_gt_proposal_" + runKey.replaceAll("[^A-Za-z0-9_.-]", "_") + suffix;
    }

    private ProblemGtCandidate toCandidate(Long projectId, String sourceRunKey, ProblemGtImportRequest.CandidatePayload payload, LocalDateTime now) {
        ProblemGtCandidate candidate = new ProblemGtCandidate();
        candidate.setProjectId(projectId);
        candidate.setSourceRunKey(sourceRunKey);
        candidate.setCandidateId(TextKeys.clean(payload.getCandidateId()));
        candidate.setNoteNo(TextKeys.clean(payload.getNoteNo()));
        candidate.setNoteName(TextKeys.clean(payload.getNoteName()));
        candidate.setFolder(TextKeys.clean(payload.getFolder()));
        candidate.setSource(TextKeys.clean(payload.getSource()));
        candidate.setBucket(TextKeys.clean(payload.getBucket()));
        candidate.setRisk(TextKeys.clean(payload.getRisk()));
        candidate.setRecommendedAction(TextKeys.clean(payload.getRecommendedAction()));
        candidate.setSuggestedIssueType(TextKeys.clean(payload.getSuggestedIssueType()));
        candidate.setReason(TextKeys.clean(payload.getReason()));
        candidate.setCandidateKind(TextKeys.clean(payload.getCandidateKind()));
        candidate.setPriority(payload.getPriority());
        candidate.setPeriodBucket(TextKeys.clean(payload.getPeriodBucket()));
        candidate.setTablePath(TextKeys.clean(payload.getTablePath()));
        candidate.setSectionPath(TextKeys.clean(payload.getSectionPath()));
        candidate.setRowLabel(TextKeys.clean(payload.getRowLabel()));
        candidate.setColumnLabel(TextKeys.clean(payload.getColumnLabel()));
        candidate.setPeriodLabel(TextKeys.clean(payload.getPeriodLabel()));
        candidate.setSourceValue(TextKeys.clean(payload.getSourceValue()));
        candidate.setTargetValue(TextKeys.clean(payload.getTargetValue()));
        candidate.setMissingCells(TextKeys.clean(payload.getMissingCells()));
        candidate.setMissingValues(TextKeys.clean(payload.getMissingValues()));
        candidate.setCurrentGtOverlap(TextKeys.clean(payload.getCurrentGtOverlap()));
        candidate.setCurrentFinalHint(TextKeys.clean(payload.getCurrentFinalHint()));
        candidate.setManualDecision(TextKeys.clean(payload.getManualDecision()));
        candidate.setManualRationale(TextKeys.clean(payload.getManualRationale()));
        candidate.setSourceLocator(TextKeys.clean(payload.getSourceLocator()));
        candidate.setTargetLocator(TextKeys.clean(payload.getTargetLocator()));
        candidate.setPdfContext(TextKeys.clean(payload.getPdfContext()));
        candidate.setRawPayloadJson(payload.getRawPayloadJson());
        candidate.setCandidateKey(TextKeys.firstNonBlank(payload.getCandidateKey(), buildCandidateKey(candidate)));
        candidate.setCreatedAt(now);
        candidate.setUpdatedAt(now);
        return candidate;
    }

    private Map<String, Object> buildExportPackage(Long projectId, String sourceRunKey) {
        ExportData data = loadExportData(projectId, sourceRunKey);
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("status", "proposal_only_not_applied");
        root.put("source_run_key", data.sourceRunKey());
        root.put("summary", data.summary());
        root.put("adds", data.adds());
        root.put("excluded", data.excluded());
        root.put("unresolved", data.unresolved());
        root.put("all_candidates", data.allRows());
        root.put("safety_note", "This proposal is read-only. It does not modify final_gt_202506.xlsx.");
        return root;
    }

    private ExportData loadExportData(Long projectId, String sourceRunKey) {
        projectService.getRequired(projectId);
        String runKey = resolveSourceRunKey(projectId, sourceRunKey);
        List<ProblemGtReviewItem> items = candidateMapper.selectExportItems(projectId, runKey);
        List<Map<String, Object>> allRows = items.stream().map(this::toExportRow).toList();
        List<Map<String, Object>> adds = allRows.stream()
                .filter(row -> List.of("ADD_TO_FINAL", "CHANGE_TYPE").contains(String.valueOf(row.get("decision"))))
                .toList();
        List<Map<String, Object>> excluded = allRows.stream()
                .filter(row -> List.of("EXCLUDE", "DUPLICATE", "TABLE_MERGE").contains(String.valueOf(row.get("decision"))))
                .toList();
        List<Map<String, Object>> unresolved = allRows.stream()
                .filter(row -> {
                    String decision = String.valueOf(row.get("decision"));
                    return decision.isBlank() || "null".equals(decision) || "NEED_RECHECK".equals(decision) || "PENDING".equals(decision);
                })
                .toList();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("source_run_key", runKey);
        summary.put("total_candidates", items.size());
        summary.put("decided_count", allRows.stream().filter(row -> !String.valueOf(row.get("decision")).isBlank()).count());
        summary.put("add_to_final_count", countDecision(items, "ADD_TO_FINAL"));
        summary.put("change_type_count", countDecision(items, "CHANGE_TYPE"));
        summary.put("exclude_count", countDecision(items, "EXCLUDE"));
        summary.put("duplicate_count", countDecision(items, "DUPLICATE"));
        summary.put("table_merge_count", countDecision(items, "TABLE_MERGE"));
        summary.put("need_recheck_count", countDecision(items, "NEED_RECHECK"));
        summary.put("pending_count", countDecision(items, "PENDING"));
        summary.put("undecided_count", items.stream().filter(item -> TextKeys.clean(item.getDecision()).isEmpty()).count());
        summary.put("proposal_add_rows", adds.size());
        summary.put("proposal_excluded_rows", excluded.size());
        summary.put("proposal_unresolved_rows", unresolved.size());
        return new ExportData(runKey, summary, adds, excluded, unresolved, allRows);
    }

    private long countDecision(List<ProblemGtReviewItem> items, String decision) {
        return items.stream().filter(item -> decision.equals(TextKeys.clean(item.getDecision()))).count();
    }

    private String resolveSourceRunKey(Long projectId, String sourceRunKey) {
        String cleaned = TextKeys.clean(sourceRunKey);
        if (!cleaned.isEmpty()) {
            return cleaned;
        }
        ProblemGtOverview overview = overview(projectId);
        String latest = TextKeys.clean(overview.getLatestSourceRunKey());
        if (latest.isEmpty()) {
            throw new IllegalArgumentException("还没有导入问题GT候选，无法导出");
        }
        return latest;
    }

    private Map<String, Object> toExportRow(ProblemGtReviewItem item) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("candidate_id", TextKeys.clean(item.getCandidateId()));
        row.put("candidate_key", TextKeys.clean(item.getCandidateKey()));
        row.put("decision", TextKeys.clean(item.getDecision()));
        row.put("final_issue_type", TextKeys.firstNonBlank(item.getDecisionIssueType(), item.getSuggestedIssueType()));
        row.put("suggested_issue_type", TextKeys.clean(item.getSuggestedIssueType()));
        row.put("note_no", TextKeys.clean(item.getNoteNo()));
        row.put("note_name", TextKeys.firstNonBlank(item.getNoteName(), item.getFolder()));
        row.put("source", TextKeys.clean(item.getSource()));
        row.put("bucket", TextKeys.clean(item.getBucket()));
        row.put("risk", TextKeys.clean(item.getRisk()));
        row.put("row_label", TextKeys.clean(item.getRowLabel()));
        row.put("column_label", TextKeys.clean(item.getColumnLabel()));
        row.put("period_label", TextKeys.clean(item.getPeriodLabel()));
        row.put("pdf_value", TextKeys.clean(item.getSourceValue()));
        row.put("excel_value", TextKeys.firstNonBlank(item.getTargetValue(), item.getMissingValues()));
        row.put("pdf_locator", TextKeys.clean(item.getSourceLocator()));
        row.put("excel_locator", TextKeys.firstNonBlank(item.getTargetLocator(), item.getMissingCells()));
        row.put("system_reason", TextKeys.clean(item.getReason()));
        row.put("current_final_hint", TextKeys.clean(item.getCurrentFinalHint()));
        row.put("manual_decision_reuse", TextKeys.clean(item.getManualDecision()));
        row.put("manual_rationale_reuse", TextKeys.clean(item.getManualRationale()));
        row.put("reviewer", TextKeys.clean(item.getReviewer()));
        row.put("review_comment", TextKeys.clean(item.getDecisionComment()));
        row.put("reviewed_at", item.getReviewedAt() == null ? "" : item.getReviewedAt().toString());
        row.put("rationale", TextKeys.firstNonBlank(item.getDecisionComment(), item.getReason(), item.getCurrentFinalHint()));
        return row;
    }

    private List<String> exportHeaders() {
        return List.of(
                "candidate_id", "decision", "final_issue_type", "note_no", "note_name",
                "row_label", "column_label", "period_label", "pdf_value", "excel_value",
                "pdf_locator", "excel_locator", "system_reason", "current_final_hint",
                "review_comment", "reviewer", "reviewed_at", "candidate_key"
        );
    }

    private List<List<String>> toSheetRows(List<Map<String, Object>> rows) {
        List<String> headers = exportHeaders();
        return rows.stream()
                .map(row -> headers.stream().map(header -> String.valueOf(row.getOrDefault(header, ""))).toList())
                .toList();
    }

    private void appendMarkdownRows(StringBuilder md, List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            md.append("暂无。\n");
            return;
        }
        for (Map<String, Object> row : rows) {
            md.append("- `").append(row.get("candidate_id")).append("` ")
                    .append("附注 ").append(row.get("note_no")).append(" ")
                    .append(row.get("note_name")).append(" / ")
                    .append(row.get("final_issue_type")).append(" / ")
                    .append(row.get("row_label")).append(" / ")
                    .append(row.get("decision")).append("\n");
        }
    }

    private record ExportData(
            String sourceRunKey,
            Map<String, Object> summary,
            List<Map<String, Object>> adds,
            List<Map<String, Object>> excluded,
            List<Map<String, Object>> unresolved,
            List<Map<String, Object>> allRows
    ) {
    }

    private String buildCandidateKey(ProblemGtCandidate candidate) {
        String joined = String.join("|",
                TextKeys.clean(candidate.getNoteNo()),
                TextKeys.clean(candidate.getSuggestedIssueType()),
                TextKeys.searchKey(candidate.getTablePath()),
                TextKeys.searchKey(candidate.getSectionPath()),
                TextKeys.searchKey(candidate.getRowLabel()),
                TextKeys.searchKey(candidate.getColumnLabel()),
                TextKeys.searchKey(candidate.getPeriodLabel()),
                TextKeys.searchKey(candidate.getSourceValue()),
                TextKeys.searchKey(candidate.getTargetValue())
        );
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(joined.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }

    private String normalizeDecision(String decision) {
        String cleaned = TextKeys.clean(decision).toUpperCase(Locale.ROOT);
        List<String> allowed = List.of("ADD_TO_FINAL", "EXCLUDE", "DUPLICATE", "CHANGE_TYPE", "TABLE_MERGE", "NEED_RECHECK", "PENDING");
        if (!allowed.contains(cleaned)) {
            throw new IllegalArgumentException("problem GT decision is not supported");
        }
        return cleaned;
    }

    private String normalizeDecisionFilter(String decision) {
        String cleaned = TextKeys.clean(decision).toUpperCase(Locale.ROOT);
        if (cleaned.isEmpty() || "ALL".equals(cleaned)) {
            return "";
        }
        if ("UNDECIDED".equals(cleaned)) {
            return cleaned;
        }
        return normalizeDecision(cleaned);
    }

    private void fillNulls(ProblemGtOverview overview) {
        if (overview.getTotalCount() == null) overview.setTotalCount(0);
        if (overview.getHighCount() == null) overview.setHighCount(0);
        if (overview.getMediumCount() == null) overview.setMediumCount(0);
        if (overview.getManualReusedCount() == null) overview.setManualReusedCount(0);
        if (overview.getCoveredCount() == null) overview.setCoveredCount(0);
        if (overview.getLockedCount() == null) overview.setLockedCount(0);
        if (overview.getPdfLowCount() == null) overview.setPdfLowCount(0);
        if (overview.getUndecidedCount() == null) overview.setUndecidedCount(0);
        if (overview.getAddCount() == null) overview.setAddCount(0);
        if (overview.getExcludeCount() == null) overview.setExcludeCount(0);
        if (overview.getDuplicateCount() == null) overview.setDuplicateCount(0);
    }
}
