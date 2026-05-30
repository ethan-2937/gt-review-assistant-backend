package com.jzyz.gtreviewassistant.domain.dto;

import com.jzyz.gtreviewassistant.domain.entity.StructureCell;
import com.jzyz.gtreviewassistant.domain.entity.StructureColumn;
import com.jzyz.gtreviewassistant.domain.entity.StructureRow;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TableView {
    private Long id;
    private String tableTitle;
    private Integer tableOrder;
    private String sourceLocator;
    private List<StructureRow> rows = new ArrayList<>();
    private List<StructureColumn> columns = new ArrayList<>();
    private List<StructureCell> cells = new ArrayList<>();
}