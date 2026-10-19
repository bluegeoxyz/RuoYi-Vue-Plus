package org.dromara.property.project.mapper;

import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.property.project.domain.ProjectMember;
import org.dromara.property.project.domain.vo.ProjectMemberVo;

import java.util.Collection;

/**
 * 物业项目成员关系数据层。
 */
public interface ProjectMemberMapper extends BaseMapperPlus<ProjectMember, ProjectMemberVo> {

    default boolean existsActive(Long projectId, Long employeeId, Long excludeId) {
        return lambda()
            .eq(ProjectMember::getProjectId, projectId)
            .eq(ProjectMember::getEmployeeId, employeeId)
            .eq(ProjectMember::getMemberStatus, "ACTIVE")
            .ne(excludeId != null, ProjectMember::getId, excludeId)
            .exists();
    }

    default int clearActiveManagers(Long projectId, Long excludeId) {
        return lambda()
            .set(ProjectMember::getIsManager, "0")
            .eq(ProjectMember::getProjectId, projectId)
            .eq(ProjectMember::getMemberStatus, "ACTIVE")
            .eq(ProjectMember::getIsManager, "1")
            .ne(excludeId != null, ProjectMember::getId, excludeId)
            .updateCount();
    }

    default boolean existsByProjectIds(Collection<Long> projectIds) {
        return projectIds != null && !projectIds.isEmpty()
            && lambda().in(ProjectMember::getProjectId, projectIds).exists();
    }

    default boolean existsByEmployeeIds(Collection<Long> employeeIds) {
        return employeeIds != null && !employeeIds.isEmpty()
            && lambda().in(ProjectMember::getEmployeeId, employeeIds).exists();
    }
}
