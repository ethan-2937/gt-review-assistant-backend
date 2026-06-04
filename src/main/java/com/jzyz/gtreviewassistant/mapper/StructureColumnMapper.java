package com.jzyz.gtreviewassistant.mapper;

import com.jzyz.gtreviewassistant.domain.entity.StructureColumn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StructureColumnMapper {
    int insert(StructureColumn column);

    int deleteByProjectAndSide(@Param("projectId") Long projectId, @Param("side") String side);

    int deleteByProjectSideNote(@Param("projectId") Long projectId,
                                @Param("side") String side,
                                @Param("noteNo") String noteNo);

    List<StructureColumn> selectByTableId(@Param("tableId") Long tableId);

    List<StructureColumn> selectByProjectNoteSide(@Param("projectId") Long projectId,
                                                   @Param("noteNo") String noteNo,
                                                   @Param("side") String side);
}
