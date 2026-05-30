package com.jzyz.gtreviewassistant.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private int pageNum;
    private int pageSize;
    private long total;
    private int pages;
    private List<T> list;
}
