package org.dromara.property.project.mapper;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.property.project.domain.ProjectManagementGrant;
import org.dromara.property.project.domain.vo.ProjectManagementGrantVo;

import java.util.Collection;
import java.util.List;

public interface ProjectManagementGrantMapper extends BaseMapperPlus<ProjectManagementGrant, ProjectManagementGrantVo> {
    default List<ProjectManagementGrant> selectEffective(String type, Collection<Long> principalIds) {
        return principalIds == null || principalIds.isEmpty() ? List.of() : lambda().eq(ProjectManagementGrant::getPrincipalType, type)
            .in(ProjectManagementGrant::getPrincipalId, principalIds).eq(ProjectManagementGrant::getStatus, "0").list();
    }

    default ProjectManagementGrant selectActiveForUpdate(String type, Long principalId, String scopeType, Long projectId) {
        return lambda().eq(ProjectManagementGrant::getPrincipalType, type).eq(ProjectManagementGrant::getPrincipalId, principalId)
            .eq(ProjectManagementGrant::getScopeType, scopeType).eq(ProjectManagementGrant::getProjectId, projectId).last("FOR UPDATE").one();
    }
}
