package org.dromara.property.project.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.property.api.ProjectAccessScope;
import org.dromara.property.api.ProjectReferenceDTO;
import org.dromara.property.api.ProjectReferenceService;
import org.dromara.property.project.domain.Project;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.scope.ProjectBusinessAction;
import org.dromara.property.scope.PropertyBusinessDomain;
import org.dromara.property.scope.PropertyProjectScope;
import org.dromara.property.scope.PropertyProjectScopeService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * Property 向其他业务模块提供的项目参考数据实现。
 */
@Service
@RequiredArgsConstructor
public class ProjectReferenceServiceImpl implements ProjectReferenceService {

    private final ProjectMapper projectMapper;
    private final PropertyProjectScopeService scopeService;

    @Override
    public ProjectAccessScope resolveWarehouseScope() {
        PropertyProjectScope scope = scopeService.resolveBusinessScope(PropertyBusinessDomain.WAREHOUSE, ProjectBusinessAction.READ);
        return new ProjectAccessScope(ProjectAccessScope.Type.valueOf(scope.type().name()), scope.projectIds());
    }

    @Override
    public ProjectReferenceDTO queryById(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        return toDto(project);
    }

    @Override
    public List<ProjectReferenceDTO> queryByIds(Collection<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return List.of();
        }
        return projectMapper.selectByIds(projectIds).stream().map(this::toDto).toList();
    }

    @Override
    public List<ProjectReferenceDTO> selectWarehouseOptions() {
        PropertyProjectScope scope = scopeService.resolveBusinessScope(PropertyBusinessDomain.WAREHOUSE, ProjectBusinessAction.OPTION);
        if (scope.type() == PropertyProjectScope.Type.NONE) {
            return List.of();
        }
        return projectMapper.lambda()
            .select(Project::getId, Project::getProjectCode, Project::getProjectName, Project::getStatus)
            .in(scope.type() == PropertyProjectScope.Type.EXPLICIT_IDS, Project::getId, scope.projectIds())
            .eq(Project::getStatus, "0")
            .orderByAsc(Project::getSort)
            .orderByAsc(Project::getId)
            .list()
            .stream()
            .map(this::toDto)
            .toList();
    }

    private ProjectReferenceDTO toDto(Project project) {
        return project == null ? null : new ProjectReferenceDTO(
            project.getId(), project.getProjectCode(), project.getProjectName(), project.getStatus());
    }
}
