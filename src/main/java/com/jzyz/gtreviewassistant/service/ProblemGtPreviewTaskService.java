package com.jzyz.gtreviewassistant.service;

import com.jzyz.gtreviewassistant.common.TextKeys;
import com.jzyz.gtreviewassistant.domain.dto.ProblemGtOverview;
import com.jzyz.gtreviewassistant.domain.dto.ProblemGtPreviewTaskRequest;
import com.jzyz.gtreviewassistant.domain.dto.ProblemGtPreviewTaskStatus;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class ProblemGtPreviewTaskService {
    private static final int OUTPUT_TAIL_LIMIT = 12000;

    private final ProjectService projectService;
    private final ProblemGtService problemGtService;
    private final Map<String, ProblemGtPreviewTaskStatus> tasks = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Value("${GT_REVIEW_WORKER_PYTHON:python}")
    private String pythonCommand;

    @Value("${GT_REVIEW_PROBLEM_GT_PREVIEW_SCRIPT:D:/audit-engine/gt-review-assistant/worker/generate_problem_gt_previews.py}")
    private String previewScriptPath;

    @Value("${GT_REVIEW_BACKEND_INTERNAL_URL:http://localhost:18081}")
    private String backendInternalUrl;

    @Value("${GT_REVIEW_PROBLEM_GT_SOURCE_ROOT:}")
    private String configuredSourceRoot;

    @Value("${GT_REVIEW_PREVIEW_OUTPUT_DIR:/workspace/preview_assets}")
    private String configuredOutputDir;

    public ProblemGtPreviewTaskStatus start(Long projectId, ProblemGtPreviewTaskRequest request) {
        projectService.getRequired(projectId);
        String sourceRunKey = TextKeys.clean(request.getSourceRunKey());
        if (sourceRunKey.isEmpty()) {
            ProblemGtOverview overview = problemGtService.overview(projectId);
            sourceRunKey = TextKeys.clean(overview.getLatestSourceRunKey());
        }
        if (sourceRunKey.isEmpty()) {
            throw new IllegalArgumentException("problem GT source run key is empty");
        }

        String sourceRoot = TextKeys.firstNonBlank(request.getSourceRoot(), configuredSourceRoot);
        if (TextKeys.clean(sourceRoot).isEmpty()) {
            throw new IllegalStateException("problem GT source root is not configured");
        }
        String outputDir = TextKeys.firstNonBlank(request.getOutputDir(), configuredOutputDir);
        int limit = request.getLimit() == null ? 0 : Math.max(request.getLimit(), 0);

        String taskId = UUID.randomUUID().toString();
        ProblemGtPreviewTaskStatus status = new ProblemGtPreviewTaskStatus();
        status.setTaskId(taskId);
        status.setStatus("QUEUED");
        status.setSourceRunKey(sourceRunKey);
        status.setMessage("queued");
        status.setCreatedAt(LocalDateTime.now());
        List<String> command = buildCommand(projectId, sourceRunKey, sourceRoot, outputDir, limit);
        status.setCommand(String.join(" ", command));
        tasks.put(taskId, status);
        executor.submit(() -> runTask(taskId, command));
        return status;
    }

    public ProblemGtPreviewTaskStatus get(String taskId) {
        ProblemGtPreviewTaskStatus status = tasks.get(taskId);
        if (status == null) {
            throw new IllegalArgumentException("preview task not found: " + taskId);
        }
        return status;
    }

    private List<String> buildCommand(Long projectId, String sourceRunKey, String sourceRoot, String outputDir, int limit) {
        List<String> command = new ArrayList<>();
        command.add(pythonCommand);
        command.add(previewScriptPath);
        command.add("--backend");
        command.add(backendInternalUrl);
        command.add("--project-id");
        command.add(String.valueOf(projectId));
        command.add("--source-run-key");
        command.add(sourceRunKey);
        command.add("--source-root");
        command.add(sourceRoot);
        command.add("--output-dir");
        command.add(outputDir);
        if (limit > 0) {
            command.add("--limit");
            command.add(String.valueOf(limit));
        }
        return command;
    }

    private void runTask(String taskId, List<String> command) {
        ProblemGtPreviewTaskStatus status = tasks.get(taskId);
        if (status == null) {
            return;
        }
        StringBuilder output = new StringBuilder();
        status.setStatus("RUNNING");
        status.setMessage("running");
        status.setStartedAt(LocalDateTime.now());
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    appendTail(output, line + System.lineSeparator());
                    status.setOutputTail(output.toString());
                }
            }
            int exitCode = process.waitFor();
            status.setExitCode(exitCode);
            status.setFinishedAt(LocalDateTime.now());
            status.setStatus(exitCode == 0 ? "SUCCESS" : "FAILED");
            status.setMessage(exitCode == 0 ? "preview images generated" : "preview worker failed");
        } catch (Exception ex) {
            appendTail(output, ex.getMessage());
            status.setOutputTail(output.toString());
            status.setStatus("FAILED");
            status.setMessage(ex.getMessage());
            status.setFinishedAt(LocalDateTime.now());
        }
    }

    private void appendTail(StringBuilder output, String value) {
        output.append(value);
        if (output.length() > OUTPUT_TAIL_LIMIT) {
            output.delete(0, output.length() - OUTPUT_TAIL_LIMIT);
        }
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }
}
