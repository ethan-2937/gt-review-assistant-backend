package com.jzyz.gtreviewassistant.domain.dto;

import com.jzyz.gtreviewassistant.domain.entity.StructureDiff;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NoteDetail {
    private String noteNo;
    private String noteName;
    private boolean pdfExists;
    private boolean excelExists;
    private List<TableView> pdfTables = new ArrayList<>();
    private List<TableView> excelTables = new ArrayList<>();
    private List<StructureDiff> diffs = new ArrayList<>();
}