package com.jzyz.gtreviewassistant.domain.dto;

import lombok.Data;

@Data
public class StructureOverview {
    private Long projectId;
    private int pdfNoteCount;
    private int excelNoteCount;
    private int bothNoteCount;
    private int pdfOnlyNoteCount;
    private int excelOnlyNoteCount;
    private int diffCount;
    private int noteDiffCount;
    private int tableDiffCount;
    private int rowDiffCount;
    private int columnDiffCount;
    private int cellDiffCount;
}