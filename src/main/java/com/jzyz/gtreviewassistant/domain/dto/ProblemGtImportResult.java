package com.jzyz.gtreviewassistant.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProblemGtImportResult {
    private String sourceRunKey;
    private int importedCount;
    private int lockedCount;
    private int highCount;
    private int mediumCount;
    private int pdfLowCount;
}
