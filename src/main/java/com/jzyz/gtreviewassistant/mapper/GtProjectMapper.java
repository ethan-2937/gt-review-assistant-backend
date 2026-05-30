package com.jzyz.gtreviewassistant.mapper;

import com.jzyz.gtreviewassistant.domain.entity.GtProject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GtProjectMapper {
    int insert(GtProject project);

    GtProject selectById(@Param("id") Long id);

    GtProject selectByProjectKey(@Param("projectKey") String projectKey);

    List<GtProject> selectAll();
}