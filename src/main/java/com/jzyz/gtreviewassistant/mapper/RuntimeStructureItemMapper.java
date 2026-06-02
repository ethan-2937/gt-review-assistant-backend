package com.jzyz.gtreviewassistant.mapper;

import com.jzyz.gtreviewassistant.domain.entity.RuntimeStructureItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RuntimeStructureItemMapper {
    void insert(RuntimeStructureItem item);

    void deleteByRunId(@Param("runId") Long runId);

    int countByRunId(@Param("runId") Long runId);
}
