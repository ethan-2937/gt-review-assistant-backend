package com.jzyz.gtreviewassistant.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProblemGtPreviewTaskStatus {
    private String taskId;
    private String status;
    private String sourceRunKey;
    private String message;
    private String command;
    private String outputTail;
    private Integer exitCode;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}
