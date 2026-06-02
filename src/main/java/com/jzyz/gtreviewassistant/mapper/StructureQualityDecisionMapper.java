package com.jzyz.gtreviewassistant.mapper;

import com.jzyz.gtreviewassistant.domain.entity.StructureQualityDecision;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StructureQualityDecisionMapper {
    void upsert(StructureQualityDecision decision);

    StructureQualityDecision selectByProjectAndIssueKey(@Param("projectId") Long projectId,
                                                        @Param("issueKey") String issueKey);
}
