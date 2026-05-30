package com.jzyz.gtreviewassistant.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImportResult {
    private String side;
    private int noteCount;
    private int tableCount;
    private int rowCount;
    private int columnCount;
    private int cellCount;
}