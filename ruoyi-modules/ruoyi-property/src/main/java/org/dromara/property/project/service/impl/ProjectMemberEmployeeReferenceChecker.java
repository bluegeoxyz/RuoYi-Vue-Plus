package org.dromara.property.project.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.hr.employee.service.EmployeeReferenceChecker;
import org.dromara.property.project.mapper.ProjectMemberMapper;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

/**
 * 项目成员历史关系对员工删除的引用保护。
 */
@Component
@RequiredArgsConstructor
public class ProjectMemberEmployeeReferenceChecker implements EmployeeReferenceChecker {

    private final ProjectMemberMapper memberMapper;

    @Override
    public Optional<String> findDeleteBlockReason(Collection<Long> employeeIds) {
        if (memberMapper.existsByEmployeeIds(employeeIds)) {
            return Optional.of("员工存在项目成员历史关系，不能删除，建议停用员工档案");
        }
        return Optional.empty();
    }
}
