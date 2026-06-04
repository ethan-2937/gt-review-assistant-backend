package com.jzyz.gtreviewassistant.mapper;

import com.jzyz.gtreviewassistant.domain.entity.StructureNote;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StructureNoteMapper {
    int insert(StructureNote note);

    int deleteByProjectAndSide(@Param("projectId") Long projectId, @Param("side") String side);

    int deleteByProjectSideNote(@Param("projectId") Long projectId,
                                @Param("side") String side,
                                @Param("noteNo") String noteNo);

    StructureNote selectById(@Param("id") Long id);

    List<StructureNote> selectByProject(@Param("projectId") Long projectId);

    List<StructureNote> selectByProjectAndSide(@Param("projectId") Long projectId, @Param("side") String side);

    List<StructureNote> selectByProjectAndNoteNo(@Param("projectId") Long projectId, @Param("noteNo") String noteNo);

    int countByProjectAndSide(@Param("projectId") Long projectId, @Param("side") String side);
}
