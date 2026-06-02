package com.jzyz.gtreviewassistant.mapper;

import com.jzyz.gtreviewassistant.domain.dto.StructureCoverageItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StructureCoverageMapper {
    List<StructureCoverageItem> selectCoverageItems(@Param("projectId") Long projectId,
                                                    @Param("noteNo") String noteNo,
                                                    @Param("level") String level,
                                                    @Param("keyword") String keyword,
                                                    @Param("runId") Long runId,
                                                    @Param("matchStatus") String matchStatus);
}
