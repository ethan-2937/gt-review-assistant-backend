package com.jzyz.gtreviewassistant.service;

import com.jzyz.gtreviewassistant.common.Side;
import com.jzyz.gtreviewassistant.common.TextKeys;
import com.jzyz.gtreviewassistant.domain.dto.NoteDetail;
import com.jzyz.gtreviewassistant.domain.dto.NoteSummary;
import com.jzyz.gtreviewassistant.domain.dto.PageResult;
import com.jzyz.gtreviewassistant.domain.dto.StructureCoverageItem;
import com.jzyz.gtreviewassistant.domain.dto.StructureCoverageSummary;
import com.jzyz.gtreviewassistant.domain.dto.StructureGtDecisionRequest;
import com.jzyz.gtreviewassistant.domain.dto.StructureQualityDecisionRequest;
import com.jzyz.gtreviewassistant.domain.dto.StructureOverview;
import com.jzyz.gtreviewassistant.domain.dto.StructureQualityIssue;
import com.jzyz.gtreviewassistant.domain.dto.TableView;
import com.jzyz.gtreviewassistant.domain.entity.StructureDiff;
import com.jzyz.gtreviewassistant.domain.entity.StructureGtDecision;
import com.jzyz.gtreviewassistant.domain.entity.StructureQualityDecision;
import com.jzyz.gtreviewassistant.domain.entity.StructureNote;
import com.jzyz.gtreviewassistant.domain.entity.StructureTable;
import com.jzyz.gtreviewassistant.mapper.StructureCoverageMapper;
import com.jzyz.gtreviewassistant.mapper.StructureCellMapper;
import com.jzyz.gtreviewassistant.mapper.StructureColumnMapper;
import com.jzyz.gtreviewassistant.mapper.StructureDiffMapper;
import com.jzyz.gtreviewassistant.mapper.StructureGtDecisionMapper;
import com.jzyz.gtreviewassistant.mapper.StructureNoteMapper;
import com.jzyz.gtreviewassistant.mapper.StructureQualityDecisionMapper;
import com.jzyz.gtreviewassistant.mapper.StructureQualityMapper;
import com.jzyz.gtreviewassistant.mapper.StructureRowMapper;
import com.jzyz.gtreviewassistant.mapper.StructureTableMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class StructureQueryService {
    private final ProjectService projectService;
    private final StructureNoteMapper noteMapper;
    private final StructureTableMapper tableMapper;
    private final StructureRowMapper rowMapper;
    private final StructureColumnMapper columnMapper;
    private final StructureCellMapper cellMapper;
    private final StructureDiffMapper diffMapper;
    private final StructureCoverageMapper coverageMapper;
    private final StructureGtDecisionMapper gtDecisionMapper;
    private final StructureQualityMapper qualityMapper;
    private final StructureQualityDecisionMapper qualityDecisionMapper;
    private final RuntimeRunService runtimeRunService;

    public StructureOverview overview(Long projectId) {
        projectService.getRequired(projectId);
        Map<String, StructureNote> pdfNotes = toMap(noteMapper.selectByProjectAndSide(projectId, Side.PDF), StructureNote::getNoteNo);
        Map<String, StructureNote> excelNotes = toMap(noteMapper.selectByProjectAndSide(projectId, Side.EXCEL), StructureNote::getNoteNo);
        Set<String> allNoteNos = union(pdfNotes.keySet(), excelNotes.keySet());

        int both = 0;
        int pdfOnly = 0;
        int excelOnly = 0;
        for (String noteNo : allNoteNos) {
            boolean hasPdf = pdfNotes.containsKey(noteNo);
            boolean hasExcel = excelNotes.containsKey(noteNo);
            if (hasPdf && hasExcel) {
                both++;
            } else if (hasPdf) {
                pdfOnly++;
            } else {
                excelOnly++;
            }
        }

        StructureOverview overview = new StructureOverview();
        overview.setProjectId(projectId);
        overview.setPdfNoteCount(pdfNotes.size());
        overview.setExcelNoteCount(excelNotes.size());
        overview.setBothNoteCount(both);
        overview.setPdfOnlyNoteCount(pdfOnly);
        overview.setExcelOnlyNoteCount(excelOnly);
        overview.setDiffCount(diffMapper.countByProject(projectId));
        overview.setNoteDiffCount(diffMapper.countByProjectAndLevel(projectId, "note"));
        overview.setTableDiffCount(diffMapper.countByProjectAndLevel(projectId, "table"));
        overview.setRowDiffCount(diffMapper.countByProjectAndLevel(projectId, "row"));
        overview.setColumnDiffCount(diffMapper.countByProjectAndLevel(projectId, "column"));
        overview.setCellDiffCount(diffMapper.countByProjectAndLevel(projectId, "cell"));
        return overview;
    }

    public List<NoteSummary> notes(Long projectId) {
        projectService.getRequired(projectId);
        Map<String, StructureNote> pdfNotes = toMap(noteMapper.selectByProjectAndSide(projectId, Side.PDF), StructureNote::getNoteNo);
        Map<String, StructureNote> excelNotes = toMap(noteMapper.selectByProjectAndSide(projectId, Side.EXCEL), StructureNote::getNoteNo);
        List<NoteSummary> summaries = new ArrayList<>();
        for (String noteNo : union(pdfNotes.keySet(), excelNotes.keySet())) {
            StructureNote pdfNote = pdfNotes.get(noteNo);
            StructureNote excelNote = excelNotes.get(noteNo);
            NoteSummary summary = new NoteSummary();
            summary.setNoteNo(noteNo);
            summary.setNoteName(TextKeys.firstNonBlank(
                    pdfNote == null ? null : pdfNote.getNoteName(),
                    excelNote == null ? null : excelNote.getNoteName()
            ));
            summary.setPdfExists(pdfNote != null);
            summary.setExcelExists(excelNote != null);
            summary.setDiffCount(diffMapper.countByProjectAndNote(projectId, noteNo));
            summaries.add(summary);
        }
        summaries.sort(Comparator.comparing(NoteSummary::getNoteNo, this::compareNoteNo));
        return summaries;
    }

    public NoteDetail noteDetail(Long projectId, String noteNo) {
        projectService.getRequired(projectId);
        List<StructureNote> notes = noteMapper.selectByProjectAndNoteNo(projectId, noteNo);
        StructureNote pdfNote = notes.stream().filter(note -> Side.PDF.equals(note.getSide())).findFirst().orElse(null);
        StructureNote excelNote = notes.stream().filter(note -> Side.EXCEL.equals(note.getSide())).findFirst().orElse(null);

        NoteDetail detail = new NoteDetail();
        detail.setNoteNo(noteNo);
        detail.setNoteName(TextKeys.firstNonBlank(
                pdfNote == null ? null : pdfNote.getNoteName(),
                excelNote == null ? null : excelNote.getNoteName()
        ));
        detail.setPdfExists(pdfNote != null);
        detail.setExcelExists(excelNote != null);
        detail.setPdfTables(buildTables(projectId, noteNo, Side.PDF));
        detail.setExcelTables(buildTables(projectId, noteNo, Side.EXCEL));
        detail.setDiffs(diffMapper.selectByProject(projectId, noteNo, null));
        return detail;
    }

    public List<StructureDiff> diffs(Long projectId, String noteNo, String diffLevel) {
        projectService.getRequired(projectId);
        return diffMapper.selectByProject(projectId, noteNo, diffLevel);
    }

    public PageResult<StructureCoverageItem> coverageItems(Long projectId,
                                                           String noteNo,
                                                           String level,
                                                           String keyword,
                                                           Long runId,
                                                           String matchStatus,
                                                           int pageNum,
                                                           int pageSize) {
        projectService.getRequired(projectId);
        if (runId != null) {
            runtimeRunService.getRequired(projectId, runId);
        }
        String safeLevel = normalizeLevel(level);
        String safeMatchStatus = normalizeMatchStatus(matchStatus);
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 5), 100);
        String cleanedKeyword = TextKeys.clean(keyword);
        String normalizedKeyword = TextKeys.searchKey(keyword);
        PageHelper.startPage(safePageNum, safePageSize);
        List<StructureCoverageItem> items = coverageMapper.selectCoverageItems(projectId, noteNo, safeLevel, cleanedKeyword, normalizedKeyword, runId, safeMatchStatus);
        PageInfo<StructureCoverageItem> pageInfo = new PageInfo<>(items);
        return new PageResult<>(
                pageInfo.getPageNum(),
                pageInfo.getPageSize(),
                pageInfo.getTotal(),
                pageInfo.getPages(),
                pageInfo.getList()
        );
    }

    public PageResult<StructureCoverageItem> runtimeOnlyItems(Long projectId,
                                                              String noteNo,
                                                              String level,
                                                              String keyword,
                                                              Long runId,
                                                              int pageNum,
                                                              int pageSize) {
        projectService.getRequired(projectId);
        String safeLevel = normalizeLevel(level);
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 5), 100);
        if (runId == null) {
            return new PageResult<>(safePageNum, safePageSize, 0, 0, List.of());
        }
        runtimeRunService.getRequired(projectId, runId);
        String cleanedKeyword = TextKeys.clean(keyword);
        String normalizedKeyword = TextKeys.searchKey(keyword);
        PageHelper.startPage(safePageNum, safePageSize);
        List<StructureCoverageItem> items = coverageMapper.selectRuntimeOnlyItems(projectId, noteNo, safeLevel, cleanedKeyword, normalizedKeyword, runId);
        PageInfo<StructureCoverageItem> pageInfo = new PageInfo<>(items);
        return new PageResult<>(
                pageInfo.getPageNum(),
                pageInfo.getPageSize(),
                pageInfo.getTotal(),
                pageInfo.getPages(),
                pageInfo.getList()
        );
    }

    public StructureCoverageSummary coverageSummary(Long projectId, String noteNo, String level, Long runId) {
        projectService.getRequired(projectId);
        if (runId != null) {
            runtimeRunService.getRequired(projectId, runId);
        }
        String safeLevel = normalizeLevel(level);
        StructureCoverageSummary summary = coverageMapper.selectCoverageSummary(projectId, noteNo, safeLevel, runId);
        if (summary == null) {
            summary = new StructureCoverageSummary();
            summary.setProjectId(projectId);
            summary.setNoteNo(noteNo);
            summary.setLevel(safeLevel);
        }
        fillSummaryNulls(summary);
        return summary;
    }

    @Transactional
    public StructureGtDecision saveGtDecision(Long projectId, StructureGtDecisionRequest request) {
        projectService.getRequired(projectId);
        LocalDateTime now = LocalDateTime.now();
        StructureGtDecision decision = new StructureGtDecision();
        decision.setProjectId(projectId);
        decision.setDecisionKey(TextKeys.clean(request.getDecisionKey()));
        decision.setViewMode(normalizeViewMode(request.getViewMode()));
        decision.setRunId(request.getRunId());
        decision.setNoteNo(TextKeys.clean(request.getNoteNo()));
        decision.setLevel(normalizeLevel(request.getLevel()));
        decision.setItemKey(TextKeys.clean(request.getItemKey()));
        decision.setRuntimeSide(TextKeys.clean(request.getRuntimeSide()).toUpperCase(Locale.ROOT));
        decision.setDecision(normalizeGtDecision(request.getDecision()));
        decision.setAliasText(TextKeys.clean(request.getAliasText()));
        decision.setMergeTargetKey(TextKeys.clean(request.getMergeTargetKey()));
        decision.setComment(TextKeys.clean(request.getComment()));
        decision.setReviewer(TextKeys.firstNonBlank(request.getReviewer(), "local-user"));
        decision.setReviewedAt(now);
        decision.setCreatedAt(now);
        decision.setUpdatedAt(now);
        gtDecisionMapper.upsert(decision);
        return gtDecisionMapper.selectByProjectAndDecisionKey(projectId, decision.getDecisionKey());
    }

    public PageResult<StructureQualityIssue> qualityIssues(Long projectId,
                                                           String noteNo,
                                                           String severity,
                                                           String issueType,
                                                           String side,
                                                           String decision,
                                                           int pageNum,
                                                           int pageSize) {
        projectService.getRequired(projectId);
        String safeNoteNo = TextKeys.clean(noteNo);
        String safeSeverity = normalizeOptionalUpper(severity, List.of("HIGH", "MEDIUM", "LOW"), "severity");
        String safeIssueType = normalizeIssueType(issueType);
        String safeSide = normalizeOptionalSide(side);
        String safeDecision = normalizeDecisionFilter(decision);
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.min(Math.max(pageSize, 5), 100);
        PageHelper.startPage(safePageNum, safePageSize);
        List<StructureQualityIssue> issues = qualityMapper.selectIssues(projectId, safeNoteNo, safeSeverity, safeIssueType, safeSide, safeDecision);
        PageInfo<StructureQualityIssue> pageInfo = new PageInfo<>(issues);
        return new PageResult<>(
                pageInfo.getPageNum(),
                pageInfo.getPageSize(),
                pageInfo.getTotal(),
                pageInfo.getPages(),
                pageInfo.getList()
        );
    }

    @Transactional
    public StructureQualityDecision saveQualityDecision(Long projectId, StructureQualityDecisionRequest request) {
        projectService.getRequired(projectId);
        LocalDateTime now = LocalDateTime.now();
        StructureQualityDecision decision = new StructureQualityDecision();
        decision.setProjectId(projectId);
        decision.setIssueKey(TextKeys.clean(request.getIssueKey()));
        decision.setIssueType(normalizeIssueType(request.getIssueType()));
        decision.setSeverity(normalizeOptionalUpper(request.getSeverity(), List.of("HIGH", "MEDIUM", "LOW"), "severity"));
        decision.setSide(normalizeOptionalSide(request.getSide()));
        decision.setNoteNo(TextKeys.clean(request.getNoteNo()));
        decision.setTableId(request.getTableId());
        decision.setTableTitle(TextKeys.clean(request.getTableTitle()));
        decision.setLevel(TextKeys.clean(request.getLevel()));
        decision.setRefId(request.getRefId());
        decision.setDecision(normalizeDecision(request.getDecision()));
        decision.setComment(TextKeys.clean(request.getComment()));
        decision.setReviewer(TextKeys.firstNonBlank(request.getReviewer(), "local-user"));
        decision.setReviewedAt(now);
        decision.setCreatedAt(now);
        decision.setUpdatedAt(now);
        qualityDecisionMapper.upsert(decision);
        return qualityDecisionMapper.selectByProjectAndIssueKey(projectId, decision.getIssueKey());
    }

    private String normalizeLevel(String level) {
        String cleaned = TextKeys.clean(level);
        if (cleaned.isEmpty()) {
            return "row";
        }
        if (!List.of("cell", "row", "column").contains(cleaned)) {
            throw new IllegalArgumentException("level 只能是 cell、row 或 column");
        }
        return cleaned;
    }

    private void fillSummaryNulls(StructureCoverageSummary summary) {
        if (summary.getSourceTotalCount() == null) summary.setSourceTotalCount(0);
        if (summary.getPdfCount() == null) summary.setPdfCount(0);
        if (summary.getExcelCount() == null) summary.setExcelCount(0);
        if (summary.getBothSideCount() == null) summary.setBothSideCount(0);
        if (summary.getPdfOnlyCount() == null) summary.setPdfOnlyCount(0);
        if (summary.getExcelOnlyCount() == null) summary.setExcelOnlyCount(0);
        if (summary.getRuntimeMatchedCount() == null) summary.setRuntimeMatchedCount(0);
        if (summary.getRuntimeMissingCount() == null) summary.setRuntimeMissingCount(0);
        if (summary.getRuntimeOnlyCount() == null) summary.setRuntimeOnlyCount(0);
    }

    private String normalizeMatchStatus(String matchStatus) {
        String cleaned = TextKeys.clean(matchStatus);
        if (cleaned.isEmpty() || "all".equalsIgnoreCase(cleaned)) {
            return "";
        }
        String lower = cleaned.toLowerCase();
        if (!List.of("matched", "missing").contains(lower)) {
            throw new IllegalArgumentException("matchStatus 只能是 all、matched 或 missing");
        }
        return lower;
    }

    private String normalizeViewMode(String viewMode) {
        String cleaned = TextKeys.clean(viewMode);
        if (cleaned.isEmpty()) {
            return "coverage";
        }
        if (!List.of("coverage", "runtimeOnly").contains(cleaned)) {
            throw new IllegalArgumentException("viewMode must be coverage or runtimeOnly");
        }
        return cleaned;
    }

    private String normalizeGtDecision(String decision) {
        String cleaned = TextKeys.clean(decision).toUpperCase(Locale.ROOT);
        List<String> allowed = List.of(
                "STRUCTURE_GT_KEEP",
                "STRUCTURE_GT_EXCLUDE",
                "STRUCTURE_GT_ALIAS",
                "STRUCTURE_GT_MERGE",
                "STRUCTURE_GT_PENDING"
        );
        if (!allowed.contains(cleaned)) {
            throw new IllegalArgumentException("decision is not supported");
        }
        return cleaned;
    }

    private String normalizeOptionalSide(String side) {
        String cleaned = TextKeys.clean(side);
        if (cleaned.isEmpty() || "all".equalsIgnoreCase(cleaned)) {
            return "";
        }
        return Side.normalize(cleaned);
    }

    private String normalizeOptionalUpper(String value, List<String> allowed, String fieldName) {
        String cleaned = TextKeys.clean(value);
        if (cleaned.isEmpty() || "all".equalsIgnoreCase(cleaned)) {
            return "";
        }
        String upper = cleaned.toUpperCase(Locale.ROOT);
        if (!allowed.contains(upper)) {
            throw new IllegalArgumentException(fieldName + " 参数不合法");
        }
        return upper;
    }

    private String normalizeIssueType(String issueType) {
        String cleaned = TextKeys.clean(issueType);
        if (cleaned.isEmpty() || "all".equalsIgnoreCase(cleaned)) {
            return "";
        }
        return cleaned.toUpperCase(Locale.ROOT);
    }

    private String normalizeDecision(String decision) {
        String cleaned = TextKeys.clean(decision).toUpperCase(Locale.ROOT);
        List<String> allowed = List.of(
                "STRUCTURE_GT_READY",
                "STRUCTURE_GT_EXCLUDED",
                "PARSER_ERROR",
                "FALSE_ALARM",
                "PENDING"
        );
        if (!allowed.contains(cleaned)) {
            throw new IllegalArgumentException("decision 参数不合法");
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

    private List<TableView> buildTables(Long projectId, String noteNo, String side) {
        List<TableView> views = new ArrayList<>();
        List<StructureTable> tables = tableMapper.selectByProjectNoteSide(projectId, noteNo, side);
        for (StructureTable table : tables) {
            TableView view = new TableView();
            view.setId(table.getId());
            view.setTableTitle(table.getTableTitle());
            view.setTableOrder(table.getTableOrder());
            view.setSourceLocator(table.getSourceLocator());
            view.setRows(rowMapper.selectByTableId(table.getId()));
            view.setColumns(columnMapper.selectByTableId(table.getId()));
            view.setCells(cellMapper.selectByTableId(table.getId()));
            views.add(view);
        }
        return views;
    }

    private <T> Map<String, T> toMap(List<T> list, Function<T, String> keyFunction) {
        Map<String, T> map = new LinkedHashMap<>();
        for (T item : list) {
            String key = keyFunction.apply(item);
            if (key != null && !key.isBlank()) {
                map.putIfAbsent(key, item);
            }
        }
        return map;
    }

    private Set<String> union(Set<String> a, Set<String> b) {
        Set<String> result = new LinkedHashSet<>();
        result.addAll(a);
        result.addAll(b);
        return result;
    }

    private int compareNoteNo(String a, String b) {
        try {
            return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
        } catch (NumberFormatException ignored) {
            return a.compareTo(b);
        }
    }
}
