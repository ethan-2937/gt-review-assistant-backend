package com.jzyz.gtreviewassistant.mapper;

import com.jzyz.gtreviewassistant.domain.entity.StructureCell;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StructureCellMapper {
    int insert(StructureCell cell);

    int deleteByProjectAndSide(@Param("projectId") Long projectId, @Param("side") String side);

    List<StructureCell> selectByTableId(@Param("tableId") Long tableId);

    List<StructureCell> selectByProjectNoteSide(@Param("projectId") Long projectId,
                                                 @Param("noteNo") String noteNo,
                                                 @Param("side") String side);
}