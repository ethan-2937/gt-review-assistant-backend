package com.jzyz.gtreviewassistant.domain.dto;

import lombok.Data;

@Data
public class NoteSummary {
    private String noteNo;
    private String noteName;
    private boolean pdfExists;
    private boolean excelExists;
    private int diffCount;
}