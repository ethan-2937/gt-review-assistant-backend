package com.jzyz.gtreviewassistant.service;

import com.jzyz.gtreviewassistant.common.Side;
import com.jzyz.gtreviewassistant.common.TextKeys;
import com.jzyz.gtreviewassistant.domain.dto.ImportResult;
import com.jzyz.gtreviewassistant.domain.dto.StructureImportRequest;
import com.jzyz.gtreviewassistant.domain.entity.StructureCell;
import com.jzyz.gtreviewassistant.domain.entity.StructureColumn;
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
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StructureImportService {
    private final ProjectService projectService;
    private final StructureNoteMapper noteMapper;
    private final StructureTableMapper tableMapper;
    private final StructureRowMapper rowMapper;
    private final StructureColumnMapper columnMapper;
    private final StructureCellMapper cellMapper;
    private final StructureDiffMapper diffMapper;

    @Transactional
    public ImportResult importStructure(Long projectId, StructureImportRequest request) {
        projectService.getRequired(projectId);
        String side = Side.normalize(request.getSide());
        deleteSide(projectId, side);
        LocalDateTime now = LocalDateTime.now();

        int noteCount = 0;
        int tableCount = 0;
        int rowCount = 0;
        int columnCount = 0;
        int cellCount = 0;

        for (StructureImportRequest.NotePayload notePayload : request.getNotes()) {
            StructureNote note = new StructureNote();
            note.setProjectId(projectId);
            note.setSide(side);
            note.setNoteNo(TextKeys.clean(notePayload.getNoteNo()));
            note.setNoteName(TextKeys.clean(notePayload.getNoteName()));
            note.setSourceFilePath(request.getSourceFilePath());
            note.setSourceLocator(notePayload.getSourceLocator());
            note.setCreatedAt(now);
            note.setUpdatedAt(now);
            noteMapper.insert(note);
            noteCount++;

            int defaultTableOrder = 1;
            for (StructureImportRequest.TablePayload tablePayload : notePayload.getTables()) {
                StructureTable table = new StructureTable();
                table.setNoteId(note.getId());
                table.setProjectId(projectId);
                table.setSide(side);
                table.setNoteNo(note.getNoteNo());
                table.setTableTitle(TextKeys.firstNonBlank(tablePayload.getTableTitle(), note.getNoteName(), "默认表"));
                table.setTableOrder(tablePayload.getTableOrder() == null ? defaultTableOrder : tablePayload.getTableOrder());
                table.setSourceLocator(tablePayload.getSourceLocator());
                table.setCreatedAt(now);
                table.setUpdatedAt(now);
                tableMapper.insert(table);
                tableCount++;
                defaultTableOrder++;

                Map<String, Long> rowIdByKey = new HashMap<>();
                int defaultRowOrder = 1;
                for (StructureImportRequest.RowPayload rowPayload : tablePayload.getRows()) {
                    StructureRow row = new StructureRow();
                    row.setTableId(table.getId());
                    row.setProjectId(projectId);
                    row.setSide(side);
                    row.setNoteNo(note.getNoteNo());
                    row.setTableTitle(table.getTableTitle());
                    row.setRowPath(TextKeys.clean(rowPayload.getRowPath()));
                    row.setRowLeaf(TextKeys.firstNonBlank(rowPayload.getRowLeaf(), rowPayload.getRowPath()));
                    row.setRowKey(TextKeys.firstNonBlank(rowPayload.getRowKey(), TextKeys.key(row.getRowPath()), TextKeys.key(row.getRowLeaf())));
                    row.setRowOrder(rowPayload.getRowOrder() == null ? defaultRowOrder : rowPayload.getRowOrder());
                    row.setSourceLocator(rowPayload.getSourceLocator());
                    row.setCreatedAt(now);
                    row.setUpdatedAt(now);
                    rowMapper.insert(row);
                    rowIdByKey.put(row.getRowKey(), row.getId());
                    rowCount++;
                    defaultRowOrder++;
                }

                Map<String, Long> columnIdByKey = new HashMap<>();
                int defaultColumnOrder = 1;
                for (StructureImportRequest.ColumnPayload columnPayload : tablePayload.getColumns()) {
                    StructureColumn column = new StructureColumn();
                    column.setTableId(table.getId());
                    column.setProjectId(projectId);
                    column.setSide(side);
                    column.setNoteNo(note.getNoteNo());
                    column.setTableTitle(table.getTableTitle());
                    column.setColumnPath(TextKeys.clean(columnPayload.getColumnPath()));
                    column.setColumnLeaf(TextKeys.firstNonBlank(columnPayload.getColumnLeaf(), columnPayload.getColumnPath()));
                    column.setColumnKey(TextKeys.firstNonBlank(columnPayload.getColumnKey(), TextKeys.key(column.getColumnPath()), TextKeys.key(column.getColumnLeaf())));
                    column.setColumnOrder(columnPayload.getColumnOrder() == null ? defaultColumnOrder : columnPayload.getColumnOrder());
                    column.setSourceLocator(columnPayload.getSourceLocator());
                    column.setCreatedAt(now);
                    column.setUpdatedAt(now);
                    columnMapper.insert(column);
                    columnIdByKey.put(column.getColumnKey(), column.getId());
                    columnCount++;
                    defaultColumnOrder++;
                }

                for (StructureImportRequest.CellPayload cellPayload : tablePayload.getCells()) {
                    String rowKey = TextKeys.firstNonBlank(cellPayload.getRowKey(), TextKeys.key(cellPayload.getRowPath()));
                    String columnKey = TextKeys.firstNonBlank(cellPayload.getColumnKey(), TextKeys.key(cellPayload.getColumnPath()));
                    StructureCell cell = new StructureCell();
                    cell.setTableId(table.getId());
                    cell.setRowId(rowIdByKey.get(rowKey));
                    cell.setColumnId(columnIdByKey.get(columnKey));
                    cell.setProjectId(projectId);
                    cell.setSide(side);
                    cell.setNoteNo(note.getNoteNo());
                    cell.setTableTitle(table.getTableTitle());
                    cell.setRowKey(rowKey);
                    cell.setColumnKey(columnKey);
                    cell.setRowPath(TextKeys.clean(cellPayload.getRowPath()));
                    cell.setColumnPath(TextKeys.clean(cellPayload.getColumnPath()));
                    cell.setValueText(TextKeys.clean(cellPayload.getValueText()));
                    cell.setNormalizedValue(TextKeys.firstNonBlank(cellPayload.getNormalizedValue(), cellPayload.getValueText()));
                    cell.setSourceLocator(cellPayload.getSourceLocator());
                    cell.setScreenshotPath(cellPayload.getScreenshotPath());
                    cell.setCreatedAt(now);
                    cell.setUpdatedAt(now);
                    cellMapper.insert(cell);
                    cellCount++;
                }
            }
        }

        return new ImportResult(side, noteCount, tableCount, rowCount, columnCount, cellCount);
    }

    private void deleteSide(Long projectId, String side) {
        diffMapper.deleteByProject(projectId);
        cellMapper.deleteByProjectAndSide(projectId, side);
        columnMapper.deleteByProjectAndSide(projectId, side);
        rowMapper.deleteByProjectAndSide(projectId, side);
        tableMapper.deleteByProjectAndSide(projectId, side);
        noteMapper.deleteByProjectAndSide(projectId, side);
    }
}