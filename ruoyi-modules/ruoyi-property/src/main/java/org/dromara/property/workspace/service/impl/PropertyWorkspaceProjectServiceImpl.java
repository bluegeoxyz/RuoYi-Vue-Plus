package org.dromara.property.workspace.service.impl;

import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.property.project.domain.Project;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.scope.ProjectBusinessAction;
import org.dromara.property.scope.PropertyBusinessDomain;
import org.dromara.property.scope.PropertyProjectScope;
import org.dromara.property.scope.PropertyProjectScopeService;
import org.dromara.property.workspace.domain.vo.WorkspaceProjectOptionVo;
import org.dromara.property.workspace.service.PropertyWorkspaceProjectService;
import org.dromara.property.workspace.service.WorkspaceProjectPermissionPolicy;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** 工作项目查询实现；仅聚合业务域范围，不替代具体业务接口的资源校验。 */
@Service
@RequiredArgsConstructor
public class PropertyWorkspaceProjectServiceImpl implements PropertyWorkspaceProjectService {

    private final PropertyProjectScopeService projectScopeService;
    private final ProjectMapper projectMapper;

    @Override
    public List<WorkspaceProjectOptionVo> selectAvailableProjects() {
        if (LoginHelper.isSuperAdmin()) {
            return queryProjects(PropertyProjectScope.all());
        }
        List<PropertyBusinessDomain> domains = WorkspaceProjectPermissionPolicy.permittedDomains(StpUtil::hasPermission);
        if (domains.isEmpty()) {
            throw new NotPermissionException("无物业工作项目业务权限");
        }

        Set<Long> projectIds = new LinkedHashSet<>();
        for (PropertyBusinessDomain domain : domains) {
            PropertyProjectScope scope = projectScopeService.resolveBusinessScope(domain, ProjectBusinessAction.OPTION);
            if (scope.type() == PropertyProjectScope.Type.ALL) {
                return queryProjects(PropertyProjectScope.all());
            }
            if (scope.type() == PropertyProjectScope.Type.EXPLICIT_IDS) {
                projectIds.addAll(scope.projectIds());
            }
        }
        return queryProjects(PropertyProjectScope.explicit(projectIds));
    }

    private List<WorkspaceProjectOptionVo> queryProjects(PropertyProjectScope scope) {
        if (scope.type() == PropertyProjectScope.Type.NONE) {
            return List.of();
        }
        Set<Long> projectIds = scope.type() == PropertyProjectScope.Type.ALL ? null : scope.projectIds();
        List<Project> projects = projectMapper.selectWorkspaceProjects(projectIds);
        return projects.stream().map(project -> new WorkspaceProjectOptionVo(
            String.valueOf(project.getId()), project.getProjectCode(), project.getProjectName(), project.getProjectShortName()
        )).toList();
    }
}
