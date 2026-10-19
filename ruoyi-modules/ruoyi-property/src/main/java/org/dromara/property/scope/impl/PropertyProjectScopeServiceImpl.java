package org.dromara.property.scope.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.satoken.utils.LoginHelper;
import org.dromara.property.scope.*;
import org.dromara.property.project.domain.ProjectManagementGrant;
import org.dromara.property.project.mapper.ProjectManagementGrantMapper;
import org.dromara.system.service.ISysRoleService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** B1：管理范围仅允许超级管理员；业务范围由 contributor 实时读取数据库。 */
@Service
@RequiredArgsConstructor
public class PropertyProjectScopeServiceImpl implements PropertyProjectScopeService {
    private final List<PropertyProjectBusinessScopeContributor> contributors;
    private final ProjectManagementGrantMapper grantMapper;
    private final ISysRoleService roleService;

    @Override
    public PropertyProjectScope resolveBusinessScope(PropertyBusinessDomain domain, ProjectBusinessAction action) {
        if (LoginHelper.isSuperAdmin()) return PropertyProjectScope.all();
        return contributors.stream().filter(c -> c.supports(domain, action)).findFirst()
            .map(c -> c.resolve(LoginHelper.getUserId(), domain, action)).orElseGet(PropertyProjectScope::none);
    }

    @Override
    public PropertyProjectScope resolveManagementScope(ProjectManagementAction action) {
        if (LoginHelper.isSuperAdmin()) return PropertyProjectScope.all();
        Long userId = LoginHelper.getUserId();
        List<ProjectManagementGrant> grants = new java.util.ArrayList<>(grantMapper.selectEffective("USER", List.of(userId)));
        List<Long> roleIds = roleService.selectRolesByUserId(userId).stream()
            .filter(role -> "0".equals(role.getStatus())).map(role -> role.getRoleId()).toList();
        grants.addAll(grantMapper.selectEffective("ROLE", roleIds));
        if (grants.stream().anyMatch(grant -> "ALL".equals(grant.getScopeType()))) return PropertyProjectScope.all();
        Set<Long> ids = grants.stream().filter(grant -> "PROJECT".equals(grant.getScopeType())).map(ProjectManagementGrant::getProjectId).collect(Collectors.toSet());
        return PropertyProjectScope.explicit(ids);
    }

    @Override
    public void assertBusinessAccess(Long projectId, PropertyBusinessDomain domain, ProjectBusinessAction action) {
        if (!resolveBusinessScope(domain, action).contains(projectId)) throw new ServiceException("无项目业务访问权限");
    }

    @Override
    public void assertManagementAccess(Long projectId, ProjectManagementAction action) {
        if (!resolveManagementScope(action).contains(projectId)) throw new ServiceException("无项目管理权限");
    }

    @Override
    public void assertCanCreateProject() {
        if (resolveManagementScope(ProjectManagementAction.CREATE_PROJECT).type() != PropertyProjectScope.Type.ALL) {
            throw new ServiceException("无项目创建管理权限");
        }
    }
}
