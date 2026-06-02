package com.jzyz.gtreviewassistant.service;

import com.jzyz.gtreviewassistant.common.TextKeys;
import com.jzyz.gtreviewassistant.domain.dto.RuntimeImportResult;
import com.jzyz.gtreviewassistant.domain.dto.RuntimeRunImportRequest;
import com.jzyz.gtreviewassistant.domain.entity.RuntimeRun;
import com.jzyz.gtreviewassistant.domain.entity.RuntimeStructureItem;
import com.jzyz.gtreviewassistant.mapper.RuntimeRunMapper;
import com.jzyz.gtreviewassistant.mapper.RuntimeStructureItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RuntimeRunService {
    private static final int ITEM_KEY_LIMIT = 2000;

    private final ProjectService projectService;
    private final RuntimeRunMapper runMapper;
    private final RuntimeStructureItemMapper itemMapper;

    public List<RuntimeRun> listRuns(Long projectId) {
        projectService.getRequired(projectId);
        return runMapper.selectByProject(projectId);
    }

    public RuntimeRun getRequired(Long projectId, Long runId) {
        projectService.getRequired(projectId);
        RuntimeRun run = runMapper.selectById(runId);
        if (run == null || !projectId.equals(run.getProjectId())) {
            throw new IllegalArgumentException("runtime run 不存在或不属于当前项目");
        }
        return run;
    }

    @Transactional
    public RuntimeImportResult importRun(Long projectId, RuntimeRunImportRequest request) {
        projectService.getRequired(projectId);
        LocalDateTime now = LocalDateTime.now();
        RuntimeRun run = runMapper.selectByProjectAndRunKey(projectId, TextKeys.clean(request.getRunKey()));
        if (run == null) {
            run = new RuntimeRun();
            run.setProjectId(projectId);
            run.setRunKey(TextKeys.clean(request.getRunKey()));
            run.setCreatedAt(now);
            copyRunFields(run, request, now);
            runMapper.insert(run);
        } else {
            itemMapper.deleteByRunId(run.getId());
            copyRunFields(run, request, now);
            runMapper.update(run);
        }

        int cellCount = 0;
        int rowCount = 0;
        int columnCount = 0;
        int pdfCount = 0;
        int excelCount = 0;
        for (RuntimeRunImportRequest.ItemPayload payload : request.getItems()) {
            RuntimeStructureItem item = toItem(projectId, run.getId(), payload, now);
            itemMapper.insert(item);
            if ("cell".equals(item.getLevel())) {
                cellCount++;
            } else if ("row".equals(item.getLevel())) {
                rowCount++;
            } else if ("column".equals(item.getLevel())) {
                columnCount++;
            }
            if ("PDF".equals(item.getRuntimeSide())) {
                pdfCount++;
            } else if ("EXCEL".equals(item.getRuntimeSide())) {
                excelCount++;
            }
        }
        return new RuntimeImportResult(run.getId(), run.getRunKey(), request.getItems().size(), cellCount, rowCount, columnCount, pdfCount, excelCount);
    }

    private void copyRunFields(RuntimeRun run, RuntimeRunImportRequest request, LocalDateTime now) {
        run.setRunRoot(TextKeys.clean(request.getRunRoot()));
        run.setRunType(defaultText(request.getRunType(), "smoke"));
        run.setRunStatus(defaultText(request.getRunStatus(), "diagnostic"));
        run.setDatasetKey(TextKeys.clean(request.getDatasetKey()));
        run.setVersionLabel(TextKeys.clean(request.getVersionLabel()));
        run.setCaseCount(request.getCaseCount());
        run.setArtifactCompleteness(defaultText(request.getArtifactCompleteness(), "SAMPLE_ONLY"));
        run.setConfidenceLevel(defaultText(request.getConfidenceLevel(), "SAMPLE"));
        run.setSourceHost(TextKeys.clean(request.getSourceHost()));
        run.setSourceSampleCount(request.getSourceSampleCount());
        run.setTargetSampleCount(request.getTargetSampleCount());
        run.setSourceStructuredCellCount(request.getSourceStructuredCellCount());
        run.setTargetStructuredCellCount(request.getTargetStructuredCellCount());
        run.setTableCount(request.getTableCount());
        run.setUpdatedAt(now);
    }

    private RuntimeStructureItem toItem(Long projectId, Long runId, RuntimeRunImportRequest.ItemPayload payload, LocalDateTime now) {
        RuntimeStructureItem item = new RuntimeStructureItem();
        item.setProjectId(projectId);
        item.setRunId(runId);
        item.setCaseId(TextKeys.clean(payload.getCaseId()));
        item.setNoteNo(TextKeys.firstNonBlank(payload.getNoteNo(), payload.getCaseId()));
        item.setNoteName(TextKeys.clean(payload.getNoteName()));
        item.setLevel(defaultText(payload.getLevel(), "cell"));
        item.setRuntimeSide(normalizeRuntimeSide(payload.getRuntimeSide()));
        item.setTableTitle(TextKeys.clean(payload.getTableTitle()));
        item.setTableProfileId(TextKeys.clean(payload.getTableProfileId()));
        item.setRowKey(TextKeys.clean(payload.getRowKey()));
        item.setRowLabel(TextKeys.clean(payload.getRowLabel()));
        item.setRowPath(TextKeys.clean(payload.getRowPath()));
        item.setColumnKey(TextKeys.clean(payload.getColumnKey()));
        item.setColumnLabel(TextKeys.clean(payload.getColumnLabel()));
        item.setColumnPath(TextKeys.clean(payload.getColumnPath()));
        item.setItemKey(TextKeys.firstNonBlank(payload.getItemKey(), buildItemKey(item)));
        item.setValueText(TextKeys.clean(payload.getValueText()));
        item.setNormalizedValue(TextKeys.clean(payload.getNormalizedValue()));
        item.setValueSignature(TextKeys.clean(payload.getValueSignature()));
        item.setValueType(TextKeys.clean(payload.getValueType()));
        item.setSourceLocator(TextKeys.clean(payload.getSourceLocator()));
        item.setCellCoordinate(TextKeys.clean(payload.getCellCoordinate()));
        item.setQuoteText(TextKeys.clean(payload.getQuoteText()));
        item.setSourceArtifact(TextKeys.clean(payload.getSourceArtifact()));
        item.setSourceJsonPath(TextKeys.clean(payload.getSourceJsonPath()));
        item.setLocatorMethod(TextKeys.clean(payload.getLocatorMethod()));
        item.setConfidenceLevel(defaultText(payload.getConfidenceLevel(), "SAMPLE"));
        item.setRawPayloadJson(payload.getRawPayloadJson());
        item.setCreatedAt(now);
        return item;
    }

    private String buildItemKey(RuntimeStructureItem item) {
        String tableTitle = TextKeys.firstNonBlank(item.getTableTitle(), "default table");
        String joined = switch (item.getLevel()) {
            case "row" -> tableTitle + "|" + TextKeys.firstNonBlank(item.getRowKey(), item.getRowPath(), item.getRowLabel());
            case "column" -> tableTitle + "|" + TextKeys.firstNonBlank(item.getColumnKey(), item.getColumnPath(), item.getColumnLabel());
            default -> tableTitle + "|"
                    + TextKeys.firstNonBlank(item.getRowKey(), item.getRowPath(), item.getRowLabel()) + "|"
                    + TextKeys.firstNonBlank(item.getColumnKey(), item.getColumnPath(), item.getColumnLabel());
        };
        return joined.length() <= ITEM_KEY_LIMIT ? joined : joined.substring(0, ITEM_KEY_LIMIT);
    }

    private String normalizeRuntimeSide(String side) {
        String cleaned = TextKeys.clean(side).toUpperCase();
        if ("SOURCE".equals(cleaned) || "PDF".equals(cleaned)) {
            return "PDF";
        }
        if ("TARGET".equals(cleaned) || "EXCEL".equals(cleaned) || "XLSX".equals(cleaned)) {
            return "EXCEL";
        }
        return cleaned.isEmpty() ? "UNKNOWN" : cleaned;
    }

    private String defaultText(String value, String fallback) {
        return TextKeys.firstNonBlank(value, fallback);
    }
}
