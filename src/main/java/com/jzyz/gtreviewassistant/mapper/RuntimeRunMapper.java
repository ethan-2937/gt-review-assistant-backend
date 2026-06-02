package com.jzyz.gtreviewassistant.mapper;

import com.jzyz.gtreviewassistant.domain.entity.RuntimeRun;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RuntimeRunMapper {
    void insert(RuntimeRun run);

    void update(RuntimeRun run);

    RuntimeRun selectById(@Param("id") Long id);

    RuntimeRun selectByProjectAndRunKey(@Param("projectId") Long projectId, @Param("runKey") String runKey);

    List<RuntimeRun> selectByProject(@Param("projectId") Long projectId);
}
