package com.jzyz.gtreviewassistant.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StructureImportRequest {
    @NotBlank(message = "side 不能为空")
    private String side;

    private String sourceFilePath;

    @Valid
    @NotEmpty(message = "notes 不能为空")
    private List<NotePayload> notes = new ArrayList<>();

    @Data
    public static class NotePayload {
        @NotBlank(message = "noteNo 不能为空")
        private String noteNo;
        private String noteName;
        private String sourceLocator;
        @Valid
        private List<TablePayload> tables = new ArrayList<>();
    }

    @Data
    public static class TablePayload {
        private String tableTitle;
        private Integer tableOrder;
        private String sourceLocator;
        @Valid
        private List<RowPayload> rows = new ArrayList<>();
        @Valid
        private List<ColumnPayload> columns = new ArrayList<>();
        @Valid
        private List<CellPayload> cells = new ArrayList<>();
    }

    @Data
    public static class RowPayload {
        private String rowKey;
        private String rowPath;
        private String rowLeaf;
        private Integer rowOrder;
        private String sourceLocator;
    }

    @Data
    public static class ColumnPayload {
        private String columnKey;
        private String columnPath;
        private String columnLeaf;
        private Integer columnOrder;
        private String sourceLocator;
    }

    @Data
    public static class CellPayload {
        private String rowKey;
        private String columnKey;
        private String rowPath;
        private String columnPath;
        private String valueText;
        private String normalizedValue;
        private String sourceLocator;
        private String screenshotPath;
    }
}