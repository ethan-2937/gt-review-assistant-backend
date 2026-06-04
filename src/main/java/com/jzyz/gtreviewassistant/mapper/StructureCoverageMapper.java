package com.jzyz.gtreviewassistant.mapper;

import com.jzyz.gtreviewassistant.domain.dto.StructureCoverageItem;
import com.jzyz.gtreviewassistant.domain.dto.StructureCoverageSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StructureCoverageMapper {
    List<StructureCoverageItem> selectCoverageItems(@Param("projectId") Long projectId,
                                                    @Param("noteNo") String noteNo,
                                                    @Param("level") String level,
                                                    @Param("keyword") String keyword,
                                                    @Param("normalizedKeyword") String normalizedKeyword,
                                                    @Param("runId") Long runId,
                                                    @Param("matchStatus") String matchStatus);

    StructureCoverageSummary selectCoverageSummary(@Param("projectId") Long projectId,
                                                   @Param("noteNo") String noteNo,
                                                   @Param("level") String level,
                                                   @Param("runId") Long runId);

    List<StructureCoverageItem> selectRuntimeOnlyItems(@Param("projectId") Long projectId,
                                                       @Param("noteNo") String noteNo,
                                                       @Param("level") String level,
                                                       @Param("keyword") String keyword,
                                                       @Param("normalizedKeyword") String normalizedKeyword,
                                                       @Param("runId") Long runId);
}
