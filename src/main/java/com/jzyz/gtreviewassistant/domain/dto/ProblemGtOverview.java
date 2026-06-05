package com.jzyz.gtreviewassistant.domain.dto;

import lombok.Data;

@Data
public class ProblemGtOverview {
    private Integer totalCount;
    private Integer highCount;
    private Integer mediumCount;
    private Integer manualReusedCount;
    private Integer coveredCount;
    private Integer lockedCount;
    private Integer pdfLowCount;
    private Integer undecidedCount;
    private Integer addCount;
    private Integer excludeCount;
    private Integer duplicateCount;
    private String latestSourceRunKey;
}
