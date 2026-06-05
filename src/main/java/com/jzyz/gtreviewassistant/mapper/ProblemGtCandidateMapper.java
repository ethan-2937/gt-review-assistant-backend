package com.jzyz.gtreviewassistant.mapper;

import com.jzyz.gtreviewassistant.domain.dto.ProblemGtOverview;
import com.jzyz.gtreviewassistant.domain.dto.ProblemGtReviewItem;
import com.jzyz.gtreviewassistant.domain.entity.ProblemGtCandidate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProblemGtCandidateMapper {
    void insert(ProblemGtCandidate candidate);

    void insertBatch(@Param("candidates") List<ProblemGtCandidate> candidates);

    void deleteByProjectAndRunKey(@Param("projectId") Long projectId, @Param("sourceRunKey") String sourceRunKey);

    ProblemGtOverview selectOverview(@Param("projectId") Long projectId);

    List<ProblemGtReviewItem> selectReviewItems(@Param("projectId") Long projectId,
                                                @Param("sourceRunKey") String sourceRunKey,
                                                @Param("bucket") String bucket,
                                                @Param("risk") String risk,
                                                @Param("noteNo") String noteNo,
                                                @Param("decision") String decision,
                                                @Param("keyword") String keyword,
                                                @Param("normalizedKeyword") String normalizedKeyword);

    List<ProblemGtReviewItem> selectExportItems(@Param("projectId") Long projectId,
                                                @Param("sourceRunKey") String sourceRunKey);

    List<ProblemGtCandidate> selectByProjectAndCandidateKeys(@Param("projectId") Long projectId,
                                                             @Param("candidateKeys") List<String> candidateKeys);
}
