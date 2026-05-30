package com.jzyz.gtreviewassistant.domain.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StructureDiff {
    private Long id;
    private Long projectId;
    private String noteNo;
    private String noteName;
    private String diffLevel;
    private String diffType;
    private String pdfRefType;
    private Long pdfRefId;
    private String excelRefType;
    private Long excelRefId;
    private String pdfText;
    private String excelText;
    private String plainSummary;
    private String impactSummary;
    private String status;
    private LocalDateTime createdAt;
}