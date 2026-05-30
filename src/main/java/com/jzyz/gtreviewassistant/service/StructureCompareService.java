package com.jzyz.gtreviewassistant.service;

import com.jzyz.gtreviewassistant.common.Side;
import com.jzyz.gtreviewassistant.common.TextKeys;
import com.jzyz.gtreviewassistant.domain.entity.StructureCell;
import com.jzyz.gtreviewassistant.domain.entity.StructureColumn;
import com.jzyz.gtreviewassistant.domain.entity.StructureDiff;
import com.jzyz.gtreviewassistant.domain.entity.StructureNote;
import com.jzyz.gtreviewassistant.domain.entity.StructureRow;
import com.jzyz.gtreviewassistant.domain.entity.StructureTable;
import com.jzyz.gtreviewassistant.mapper.StructureCellMapper;
import com.jzyz.gtreviewassistant.mapper.StructureColumnMapper;
import com.jzyz.gtreviewassistant.mapper.StructureDiffMapper;
import com.jzyz.gtreviewassistant.mapper.StructureNoteMapper;
import com.jzyz.gtreviewassistant.mapper.StructureRowMapper;
import com.jzyz.gtreviewassistant.mapper.StructureTableMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class StructureCompareService {
    private final ProjectService projectService;
    private final StructureNoteMapper noteMapper;
    private final StructureTableMapper tableMapper;
    private final StructureRowMapper rowMapper;
    private final StructureColumnMapper columnMapper;
    private final StructureCellMapper cellMapper;
    private final StructureDiffMapper diffMapper;

    @Transactional
    public int rebuildDiffs(Long projectId) {
        projectService.getRequired(projectId);
        diffMapper.deleteByProject(projectId);

        Map<String, StructureNote> pdfNotes = toMap(noteMapper.selectByProjectAndSide(projectId, Side.PDF), StructureNote::getNoteNo);
        Map<String, StructureNote> excelNotes = toMap(noteMapper.selectByProjectAndSide(projectId, Side.EXCEL), StructureNote::getNoteNo);
        Set<String> noteNos = union(pdfNotes.keySet(), excelNotes.keySet());

        int count = 0;
        for (String noteNo : noteNos) {
            StructureNote pdfNote = pdfNotes.get(noteNo);
            StructureNote excelNote = excelNotes.get(noteNo);
            String noteName = TextKeys.firstNonBlank(
                    pdfNote == null ? null : pdfNote.getNoteName(),
                    excelNote == null ? null : excelNote.getNoteName()
            );
            if (pdfNote == null) {
                insertDiff(projectId, noteNo, noteName, "note", "only_in_excel", null, null, "note", excelNote.getId(),
                        "PDF：没有这个附注", "Excel：有附注" + noteLabel(noteNo, noteName),
                        "Excel 有这个附注，PDF 没有：" + noteLabel(noteNo, noteName),
                        "如果原文确认 PDF 确实没有，后续可能形成缺附注/缺表类候选。它现在只是结构线索。",
                        "OPEN");
                count++;
                continue;
            }
            if (excelNote == null) {
                insertDiff(projectId, noteNo, noteName, "note", "only_in_pdf", "note", pdfNote.getId(), null, null,
                        "PDF：有附注" + noteLabel(noteNo, noteName), "Excel：没有这个附注",
                        "PDF 有这个附注，Excel 没有：" + noteLabel(noteNo, noteName),
                        "如果原文确认 Excel 确实没有，后续可能形成多附注/多表类候选。它现在只是结构线索。",
                        "OPEN");
                count++;
                continue;
            }
            count += compareTables(projectId, noteNo, noteName);
            count += compareRows(projectId, noteNo, noteName);
            count += compareColumns(projectId, noteNo, noteName);
            count += compareCells(projectId, noteNo, noteName);
        }
        return count;
    }

    private int compareTables(Long projectId, String noteNo, String noteName) {
        Map<String, StructureTable> pdf = toMap(tableMapper.selectByProjectNoteSide(projectId, noteNo, Side.PDF), this::tableKey);
        Map<String, StructureTable> excel = toMap(tableMapper.selectByProjectNoteSide(projectId, noteNo, Side.EXCEL), this::tableKey);
        int count = 0;
        for (String key : union(pdf.keySet(), excel.keySet())) {
            StructureTable pdfTable = pdf.get(key);
            StructureTable excelTable = excel.get(key);
            if (pdfTable == null) {
                insertDiff(projectId, noteNo, noteName, "table", "only_in_excel", null, null, "table", excelTable.getId(),
                        "PDF：没有这个表/小节", "Excel：" + excelTable.getTableTitle(),
                        "Excel 有这个表/小节，PDF 没有：" + excelTable.getTableTitle(),
                        "后续按表对齐时，这个表下的行、列、单元格可能都无法一一对应。",
                        "OPEN");
                count++;
            } else if (excelTable == null) {
                insertDiff(projectId, noteNo, noteName, "table", "only_in_pdf", "table", pdfTable.getId(), null, null,
                        "PDF：" + pdfTable.getTableTitle(), "Excel：没有这个表/小节",
                        "PDF 有这个表/小节，Excel 没有：" + pdfTable.getTableTitle(),
                        "后续按表对齐时，这个表下的行、列、单元格可能都无法一一对应。",
                        "OPEN");
                count++;
            }
        }
        return count;
    }

    private int compareRows(Long projectId, String noteNo, String noteName) {
        Map<String, StructureRow> pdf = toMap(rowMapper.selectByProjectNoteSide(projectId, noteNo, Side.PDF), this::rowCompareKey);
        Map<String, StructureRow> excel = toMap(rowMapper.selectByProjectNoteSide(projectId, noteNo, Side.EXCEL), this::rowCompareKey);
        int count = 0;
        for (String key : union(pdf.keySet(), excel.keySet())) {
            StructureRow pdfRow = pdf.get(key);
            StructureRow excelRow = excel.get(key);
            if (pdfRow == null) {
                insertDiff(projectId, noteNo, noteName, "row", "only_in_excel", null, null, "row", excelRow.getId(),
                        "PDF：没有这行", "Excel：" + displayRow(excelRow),
                        "Excel 有这行，PDF 没有：" + displayRow(excelRow),
                        "如果原文确认 PDF 确实没有这行，后续可能形成 row:missing 候选。",
                        "OPEN");
                count++;
            } else if (excelRow == null) {
                insertDiff(projectId, noteNo, noteName, "row", "only_in_pdf", "row", pdfRow.getId(), null, null,
                        "PDF：" + displayRow(pdfRow), "Excel：没有这行",
                        "PDF 有这行，Excel 没有：" + displayRow(pdfRow),
                        "如果原文确认 Excel 确实没有这行，后续可能形成 row:extra 候选。",
                        "OPEN");
                count++;
            }
        }
        return count;
    }

    private int compareColumns(Long projectId, String noteNo, String noteName) {
        Map<String, StructureColumn> pdf = toMap(columnMapper.selectByProjectNoteSide(projectId, noteNo, Side.PDF), this::columnCompareKey);
        Map<String, StructureColumn> excel = toMap(columnMapper.selectByProjectNoteSide(projectId, noteNo, Side.EXCEL), this::columnCompareKey);
        int count = 0;
        for (String key : union(pdf.keySet(), excel.keySet())) {
            StructureColumn pdfColumn = pdf.get(key);
            StructureColumn excelColumn = excel.get(key);
            if (pdfColumn == null) {
                insertDiff(projectId, noteNo, noteName, "column", "only_in_excel", null, null, "column", excelColumn.getId(),
                        "PDF：没有这个列名", "Excel：" + displayColumn(excelColumn),
                        "Excel 有这个列名，PDF 没有：" + displayColumn(excelColumn),
                        "如果原文确认 PDF 确实没有这个列名，后续可能形成 col:missing 候选。",
                        "OPEN");
                count++;
            } else if (excelColumn == null) {
                insertDiff(projectId, noteNo, noteName, "column", "only_in_pdf", "column", pdfColumn.getId(), null, null,
                        "PDF：" + displayColumn(pdfColumn), "Excel：没有这个列名",
                        "PDF 有这个列名，Excel 没有：" + displayColumn(pdfColumn),
                        "如果原文确认 Excel 确实没有这个列名，后续可能形成 col:extra 候选。",
                        "OPEN");
                count++;
            }
        }
        return count;
    }

    private int compareCells(Long projectId, String noteNo, String noteName) {
        Map<String, StructureCell> pdf = toMap(cellMapper.selectByProjectNoteSide(projectId, noteNo, Side.PDF), this::cellCompareKey);
        Map<String, StructureCell> excel = toMap(cellMapper.selectByProjectNoteSide(projectId, noteNo, Side.EXCEL), this::cellCompareKey);
        int count = 0;
        for (String key : union(pdf.keySet(), excel.keySet())) {
            StructureCell pdfCell = pdf.get(key);
            StructureCell excelCell = excel.get(key);
            if (pdfCell == null) {
                insertDiff(projectId, noteNo, noteName, "cell", "only_in_excel", null, null, "cell", excelCell.getId(),
                        "PDF：没有这个数据格", "Excel：" + displayCell(excelCell),
                        "Excel 有这个数据格，PDF 没有：" + displayCell(excelCell),
                        "先确认是否是同一张表、同一行、同一列；确认缺失后才进入 GT 候选。",
                        "OPEN");
                count++;
            } else if (excelCell == null) {
                insertDiff(projectId, noteNo, noteName, "cell", "only_in_pdf", "cell", pdfCell.getId(), null, null,
                        "PDF：" + displayCell(pdfCell), "Excel：没有这个数据格",
                        "PDF 有这个数据格，Excel 没有：" + displayCell(pdfCell),
                        "先确认是否是同一张表、同一行、同一列；确认缺失后才进入 GT 候选。",
                        "OPEN");
                count++;
            } else if (!Objects.equals(valueKey(pdfCell), valueKey(excelCell))) {
                insertDiff(projectId, noteNo, noteName, "cell", "value_diff", "cell", pdfCell.getId(), "cell", excelCell.getId(),
                        "PDF：" + displayCell(pdfCell), "Excel：" + displayCell(excelCell),
                        "同一行列的数据值不一致：PDF 是 " + valueText(pdfCell) + "，Excel 是 " + valueText(excelCell),
                        "如果原文定位正确，后续可能形成 cell:value_mismatch 候选。",
                        "OPEN");
                count++;
            }
        }
        return count;
    }

    private <T> Map<String, T> toMap(List<T> list, Function<T, String> keyFunction) {
        Map<String, T> map = new LinkedHashMap<>();
        for (T item : list) {
            String key = TextKeys.key(keyFunction.apply(item));
            if (!key.isEmpty()) {
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

    private String tableKey(StructureTable table) {
        String title = TextKeys.firstNonBlank(table.getTableTitle(), "默认表");
        return title + "#" + (table.getTableOrder() == null ? "" : table.getTableOrder());
    }

    private String rowCompareKey(StructureRow row) {
        return tableTitlePart(row.getTableTitle()) + "|" + TextKeys.firstNonBlank(row.getRowKey(), row.getRowPath(), row.getRowLeaf());
    }

    private String columnCompareKey(StructureColumn column) {
        return tableTitlePart(column.getTableTitle()) + "|" + TextKeys.firstNonBlank(column.getColumnKey(), column.getColumnPath(), column.getColumnLeaf());
    }

    private String cellCompareKey(StructureCell cell) {
        return tableTitlePart(cell.getTableTitle()) + "|" + cell.getRowKey() + "|" + cell.getColumnKey();
    }

    private String tableTitlePart(String tableTitle) {
        return TextKeys.firstNonBlank(tableTitle, "默认表");
    }

    private String valueKey(StructureCell cell) {
        return TextKeys.key(TextKeys.firstNonBlank(cell.getNormalizedValue(), cell.getValueText()));
    }

    private String valueText(StructureCell cell) {
        return TextKeys.firstNonBlank(cell.getValueText(), cell.getNormalizedValue(), "空值");
    }

    private String displayRow(StructureRow row) {
        return TextKeys.firstNonBlank(row.getRowPath(), row.getRowLeaf(), row.getRowKey());
    }

    private String displayColumn(StructureColumn column) {
        return TextKeys.firstNonBlank(column.getColumnPath(), column.getColumnLeaf(), column.getColumnKey());
    }

    private String displayCell(StructureCell cell) {
        return "行：" + TextKeys.firstNonBlank(cell.getRowPath(), cell.getRowKey())
                + "；列：" + TextKeys.firstNonBlank(cell.getColumnPath(), cell.getColumnKey())
                + "；值：" + valueText(cell);
    }

    private String noteLabel(String noteNo, String noteName) {
        return TextKeys.clean("附注" + noteNo + " " + TextKeys.clean(noteName));
    }

    private void insertDiff(Long projectId, String noteNo, String noteName, String diffLevel, String diffType,
                            String pdfRefType, Long pdfRefId, String excelRefType, Long excelRefId,
                            String pdfText, String excelText, String plainSummary, String impactSummary, String status) {
        StructureDiff diff = new StructureDiff();
        diff.setProjectId(projectId);
        diff.setNoteNo(noteNo);
        diff.setNoteName(noteName);
        diff.setDiffLevel(diffLevel);
        diff.setDiffType(diffType);
        diff.setPdfRefType(pdfRefType);
        diff.setPdfRefId(pdfRefId);
        diff.setExcelRefType(excelRefType);
        diff.setExcelRefId(excelRefId);
        diff.setPdfText(pdfText);
        diff.setExcelText(excelText);
        diff.setPlainSummary(plainSummary);
        diff.setImpactSummary(impactSummary);
        diff.setStatus(status);
        diff.setCreatedAt(LocalDateTime.now());
        diffMapper.insert(diff);
    }
}