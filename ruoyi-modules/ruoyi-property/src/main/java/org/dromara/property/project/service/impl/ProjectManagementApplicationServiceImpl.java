package org.dromara.property.project.service.impl;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.property.project.domain.bo.CreateManagedProjectCommand;
import org.dromara.property.project.domain.bo.ProjectMemberBo;
import org.dromara.property.project.service.IProjectMemberService;
import org.dromara.property.project.service.IProjectService;
import org.dromara.property.project.service.ProjectManagementApplicationService;
import org.dromara.property.scope.PropertyProjectScopeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectManagementApplicationServiceImpl implements ProjectManagementApplicationService {
    private final PropertyProjectScopeService scopeService;
    private final IProjectService projectService;
    private final IProjectMemberService memberService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createManagedProject(CreateManagedProjectCommand c) {
        if (c == null || c.getProject() == null) throw new ServiceException("项目参数不能为空");
        scopeService.assertCanCreateProject();
        c.getProject().setManagerEmployeeId(null);
        projectService.insertByBo(c.getProject());
        Long projectId = c.getProject().getId();
        List<ProjectMemberBo> members = c.getInitialMembers() == null ? List.of() : c.getInitialMembers();
        for (ProjectMemberBo member : members) {
            member.setProjectId(projectId);
            if (c.getManagerEmployeeId() != null && c.getManagerEmployeeId().equals(member.getEmployeeId()))
                member.setIsManager("1");
            memberService.addMember(member);
        }
        if (c.getManagerEmployeeId() != null && members.stream().noneMatch(m -> c.getManagerEmployeeId().equals(m.getEmployeeId())))
            throw new ServiceException("负责人必须属于首批成员");
        return true;
    }
}
