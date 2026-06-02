package com.jzyz.gtreviewassistant.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RuntimeImportResult {
    private Long runId;
    private String runKey;
    private int itemCount;
    private int cellCount;
    private int rowCount;
    private int columnCount;
    private int pdfCount;
    private int excelCount;
}
