package com.jzyz.gtreviewassistant.mapper;

import com.jzyz.gtreviewassistant.domain.entity.StructureTable;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StructureTableMapper {
    int insert(StructureTable table);

    int deleteByProjectAndSide(@Param("projectId") Long projectId, @Param("side") String side);

    int deleteByProjectSideNote(@Param("projectId") Long projectId,
                                @Param("side") String side,
                                @Param("noteNo") String noteNo);

    StructureTable selectById(@Param("id") Long id);

    List<StructureTable> selectByProjectNoteSide(@Param("projectId") Long projectId,
                                                  @Param("noteNo") String noteNo,
                                                  @Param("side") String side);

    List<StructureTable> selectByProjectAndSide(@Param("projectId") Long projectId,
                                                 @Param("side") String side);
}
