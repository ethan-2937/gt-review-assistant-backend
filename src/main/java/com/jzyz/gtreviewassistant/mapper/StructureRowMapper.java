package com.jzyz.gtreviewassistant.mapper;

import com.jzyz.gtreviewassistant.domain.entity.StructureRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StructureRowMapper {
    int insert(StructureRow row);

    int deleteByProjectAndSide(@Param("projectId") Long projectId, @Param("side") String side);

    int deleteByProjectSideNote(@Param("projectId") Long projectId,
                                @Param("side") String side,
                                @Param("noteNo") String noteNo);

    List<StructureRow> selectByTableId(@Param("tableId") Long tableId);

    List<StructureRow> selectByProjectNoteSide(@Param("projectId") Long projectId,
                                                @Param("noteNo") String noteNo,
                                                @Param("side") String side);
}
