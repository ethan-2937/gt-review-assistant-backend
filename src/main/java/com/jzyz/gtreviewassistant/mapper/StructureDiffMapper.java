package com.jzyz.gtreviewassistant.mapper;

import com.jzyz.gtreviewassistant.domain.entity.StructureDiff;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StructureDiffMapper {
    int insert(StructureDiff diff);

    int deleteByProject(@Param("projectId") Long projectId);

    int deleteByProjectAndNote(@Param("projectId") Long projectId, @Param("noteNo") String noteNo);

    List<StructureDiff> selectByProject(@Param("projectId") Long projectId,
                                         @Param("noteNo") String noteNo,
                                         @Param("diffLevel") String diffLevel);

    int countByProject(@Param("projectId") Long projectId);

    int countByProjectAndLevel(@Param("projectId") Long projectId, @Param("diffLevel") String diffLevel);

    int countByProjectAndNote(@Param("projectId") Long projectId, @Param("noteNo") String noteNo);
}
