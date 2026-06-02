package com.jzyz.gtreviewassistant.mapper;

import com.jzyz.gtreviewassistant.domain.dto.StructureQualityIssue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StructureQualityMapper {
    List<StructureQualityIssue> selectIssues(@Param("projectId") Long projectId,
                                             @Param("noteNo") String noteNo,
                                             @Param("severity") String severity,
                                             @Param("issueType") String issueType,
                                             @Param("side") String side,
                                             @Param("decision") String decision);
}
