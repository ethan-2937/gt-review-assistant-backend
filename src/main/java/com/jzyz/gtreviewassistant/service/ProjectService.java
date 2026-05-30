package com.jzyz.gtreviewassistant.service;

import com.jzyz.gtreviewassistant.domain.dto.ProjectCreateRequest;
import com.jzyz.gtreviewassistant.domain.entity.GtProject;
import com.jzyz.gtreviewassistant.mapper.GtProjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final GtProjectMapper projectMapper;

    public GtProject create(ProjectCreateRequest request) {
        GtProject exists = projectMapper.selectByProjectKey(request.getProjectKey());
        if (exists != null) {
            throw new IllegalArgumentException("项目 key 已存在：" + request.getProjectKey());
        }
        LocalDateTime now = LocalDateTime.now();
        GtProject project = new GtProject();
        project.setProjectKey(request.getProjectKey());
        project.setName(request.getName());
        project.setYearLabel(request.getYearLabel());
        project.setDatasetKey(request.getDatasetKey());
        project.setVersionLabel(request.getVersionLabel());
        project.setStatus("ACTIVE");
        project.setCreatedBy(request.getCreatedBy());
        project.setCreatedAt(now);
        project.setUpdatedAt(now);
        projectMapper.insert(project);
        return project;
    }

    public List<GtProject> list() {
        return projectMapper.selectAll();
    }

    public GtProject getRequired(Long id) {
        GtProject project = projectMapper.selectById(id);
        if (project == null) {
            throw new IllegalArgumentException("项目不存在：" + id);
        }
        return project;
    }
}