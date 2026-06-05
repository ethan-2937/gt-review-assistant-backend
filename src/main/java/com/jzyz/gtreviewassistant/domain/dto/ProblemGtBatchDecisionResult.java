package com.jzyz.gtreviewassistant.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProblemGtBatchDecisionResult {
    private int requestedCount;
    private int matchedCount;
    private int savedCount;
    private int skippedCount;
}
