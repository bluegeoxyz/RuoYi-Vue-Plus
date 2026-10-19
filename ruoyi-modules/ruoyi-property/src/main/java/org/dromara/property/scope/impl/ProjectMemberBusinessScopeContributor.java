package org.dromara.property.scope.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.hr.employee.service.IEmployeeService;
import org.dromara.property.project.domain.ProjectMember;
import org.dromara.property.project.mapper.ProjectMemberMapper;
import org.dromara.property.scope.*;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/** 当前仅以有效项目成员关系提供基础项目业务范围。 */
@Component
@RequiredArgsConstructor
public class ProjectMemberBusinessScopeContributor implements PropertyProjectBusinessScopeContributor {
    private final IEmployeeService employeeService;
    private final ProjectMemberMapper memberMapper;

    @Override
    public boolean supports(PropertyBusinessDomain domain, ProjectBusinessAction action) {
        return domain == PropertyBusinessDomain.PROJECT_BASE || domain == PropertyBusinessDomain.RESOURCE
            || domain == PropertyBusinessDomain.CUSTOMER_RELATION || domain == PropertyBusinessDomain.FINANCE
            || domain == PropertyBusinessDomain.WAREHOUSE;
    }

    @Override
    public PropertyProjectScope resolve(Long userId, PropertyBusinessDomain domain, ProjectBusinessAction action) {
        Long employeeId = employeeService.findActiveEmployeeIdByUserId(userId);
        if (employeeId == null) {
            return PropertyProjectScope.none();
        }
        Set<Long> ids = memberMapper.lambda().select(ProjectMember::getProjectId)
            .eq(ProjectMember::getEmployeeId, employeeId).eq(ProjectMember::getMemberStatus, "ACTIVE")
            .eq(ProjectMember::getStatus, "0").list().stream().map(ProjectMember::getProjectId).collect(Collectors.toSet());
        return PropertyProjectScope.explicit(ids);
    }
}
