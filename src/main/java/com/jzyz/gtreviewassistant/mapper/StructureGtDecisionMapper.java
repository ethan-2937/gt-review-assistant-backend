package com.jzyz.gtreviewassistant.mapper;

import com.jzyz.gtreviewassistant.domain.entity.StructureGtDecision;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StructureGtDecisionMapper {
    void upsert(StructureGtDecision decision);

    StructureGtDecision selectByProjectAndDecisionKey(@Param("projectId") Long projectId,
                                                      @Param("decisionKey") String decisionKey);
}
