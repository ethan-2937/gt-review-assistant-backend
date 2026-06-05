package com.jzyz.gtreviewassistant.mapper;

import com.jzyz.gtreviewassistant.domain.entity.ProblemGtDecision;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProblemGtDecisionMapper {
    void upsert(ProblemGtDecision decision);

    ProblemGtDecision selectByProjectAndCandidateKey(@Param("projectId") Long projectId,
                                                     @Param("candidateKey") String candidateKey);
}
