package org.dromara.property.project.service.impl;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.service.DictService;
import org.dromara.hr.employee.domain.vo.EmployeeVo;
import org.dromara.hr.employee.service.IEmployeeService;
import org.dromara.property.project.domain.Project;
import org.dromara.property.project.domain.ProjectMember;
import org.dromara.property.project.domain.bo.ProjectMemberBo;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.project.mapper.ProjectMemberMapper;
import org.dromara.property.scope.PropertyProjectScope;
import org.dromara.property.scope.PropertyProjectScopeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 物业项目成员关系服务单元测试。 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProjectMemberServiceImplTest {

    @Mock
    private ProjectMemberMapper memberMapper;
    @Mock
    private ProjectMapper projectMapper;
    @Mock
    private IEmployeeService employeeService;
    @Mock
    private DictService dictService;
    @Mock
    private PropertyProjectScopeService scopeService;
    @InjectMocks
    private ProjectMemberServiceImpl service;

    @BeforeEach
    void setUp() {
        when(scopeService.resolveManagementScope(any())).thenReturn(PropertyProjectScope.all());
        when(dictService.getAllDictByDictType("pm_member_role")).thenReturn(Map.of("MEMBER", "普通成员", "PROJECT_MANAGER", "项目负责人"));
        when(dictService.getAllDictByDictType("pm_member_status")).thenReturn(Map.of("ACTIVE", "有效", "INACTIVE", "已结束"));
        when(dictService.getAllDictByDictType("sys_normal_disable")).thenReturn(Map.of("0", "正常", "1", "停用"));
        when(projectMapper.selectByIdForUpdate(1L)).thenReturn(project("0", null));
        when(employeeService.queryById(2L)).thenReturn(employee("ACTIVE", "0"));
        when(projectMapper.updateManagerEmployeeId(any(), any())).thenReturn(1);
    }

    @Test
    void validEmployeeShouldJoinValidProject() {
        ProjectMemberBo bo = validBo();
        doAnswer(invocation -> {
            ProjectMember member = invocation.getArgument(0);
            member.setId(10L);
            return 1;
        }).when(memberMapper).insert(any(ProjectMember.class));

        assertTrue(service.addMember(bo));
        assertEquals(10L, bo.getId());
    }

    @Test
    void duplicateActiveMemberShouldFailAfterProjectLock() {
        when(memberMapper.existsActive(1L, 2L, null)).thenReturn(true);
        assertThrows(ServiceException.class, () -> service.addMember(validBo()));
        InOrder order = inOrder(projectMapper, memberMapper);
        order.verify(projectMapper).selectByIdForUpdate(1L);
        order.verify(memberMapper).existsActive(1L, 2L, null);
        verify(memberMapper, never()).insert(any(ProjectMember.class));
    }

    @Test
    void leftOrDisabledEmployeeCannotJoin() {
        when(employeeService.queryById(2L)).thenReturn(employee("LEFT", "0"));
        assertThrows(ServiceException.class, () -> service.addMember(validBo()));
        when(employeeService.queryById(2L)).thenReturn(employee("ACTIVE", "1"));
        assertThrows(ServiceException.class, () -> service.addMember(validBo()));
    }

    @Test
    void disabledProjectCannotAddMember() {
        when(projectMapper.selectByIdForUpdate(1L)).thenReturn(project("1", null));
        assertThrows(ServiceException.class, () -> service.addMember(validBo()));
        verify(employeeService, never()).queryById(2L);
    }

    @Test
    void activeMemberRoleCanBeUpdatedWithoutChangingSubjects() {
        ProjectMember current = activeMember("0");
        when(memberMapper.selectById(9L)).thenReturn(current);
        when(memberMapper.updateById(any(ProjectMember.class))).thenReturn(1);
        ProjectMemberBo bo = validBo();
        bo.setId(9L);
        bo.setProjectId(null);
        bo.setEmployeeId(null);
        bo.setMemberRole("PROJECT_MANAGER");

        assertTrue(service.updateMember(bo));
        verify(memberMapper).updateById(any(ProjectMember.class));
    }

    @Test
    void historicalMemberCannotBeReactivatedButCanJoinAsNewRecord() {
        ProjectMember historical = activeMember("0");
        historical.setMemberStatus("INACTIVE");
        historical.setEndDate(LocalDate.of(2026, 1, 31));
        when(memberMapper.selectById(9L)).thenReturn(historical);
        ProjectMemberBo update = validBo();
        update.setId(9L);
        assertThrows(ServiceException.class, () -> service.updateMember(update));

        when(memberMapper.existsActive(1L, 2L, null)).thenReturn(false);
        when(memberMapper.insert(any(ProjectMember.class))).thenReturn(1);
        assertTrue(service.addMember(validBo()));
    }

    @Test
    void endDateBeforeStartDateShouldFail() {
        ProjectMember current = activeMember("0");
        when(memberMapper.selectById(9L)).thenReturn(current);
        assertThrows(ServiceException.class,
            () -> service.endMember(9L, current.getStartDate().minusDays(1)));
        verify(memberMapper, never()).updateById(any(ProjectMember.class));
    }

    @Test
    void settingNewManagerShouldSerializeAndSynchronizeProject() {
        ProjectMember member = activeMember("0");
        when(memberMapper.selectById(9L)).thenReturn(member);
        when(memberMapper.updateById(any(ProjectMember.class))).thenReturn(1);

        assertTrue(service.setManager(9L));
        InOrder order = inOrder(projectMapper, memberMapper);
        order.verify(projectMapper).selectByIdForUpdate(1L);
        order.verify(memberMapper).clearActiveManagers(1L, 9L);
        order.verify(memberMapper).updateById(any(ProjectMember.class));
        order.verify(projectMapper).updateManagerEmployeeId(1L, 2L);
    }

    @Test
    void endingManagerShouldClearProjectManagerAndKeepHistory() {
        ProjectMember manager = activeMember("1");
        when(memberMapper.selectById(9L)).thenReturn(manager);
        when(projectMapper.selectByIdForUpdate(1L)).thenReturn(project("0", 2L));
        when(memberMapper.updateById(any(ProjectMember.class))).thenReturn(1);

        assertTrue(service.endMember(9L, LocalDate.of(2026, 9, 25)));
        verify(projectMapper).updateManagerEmployeeId(1L, null);
        verify(memberMapper, never()).deleteById(any());
    }

    @Test
    void endingOrdinaryMemberShouldNotClearAnotherManager() {
        ProjectMember member = activeMember("0");
        when(memberMapper.selectById(9L)).thenReturn(member);
        when(projectMapper.selectByIdForUpdate(1L)).thenReturn(project("0", 3L));
        when(memberMapper.updateById(any(ProjectMember.class))).thenReturn(1);

        assertTrue(service.endMember(9L, LocalDate.of(2026, 9, 25)));
        verify(projectMapper, never()).updateManagerEmployeeId(any(), any());
    }

    private ProjectMemberBo validBo() {
        ProjectMemberBo bo = new ProjectMemberBo();
        bo.setProjectId(1L);
        bo.setEmployeeId(2L);
        bo.setMemberRole("MEMBER");
        bo.setIsManager("0");
        bo.setStartDate(LocalDate.of(2026, 9, 1));
        bo.setMemberStatus("ACTIVE");
        bo.setStatus("0");
        bo.setSort(0);
        return bo;
    }

    private ProjectMember activeMember(String isManager) {
        ProjectMember member = new ProjectMember();
        member.setId(9L);
        member.setProjectId(1L);
        member.setEmployeeId(2L);
        member.setMemberRole("MEMBER");
        member.setIsManager(isManager);
        member.setStartDate(LocalDate.of(2026, 9, 1));
        member.setMemberStatus("ACTIVE");
        member.setStatus("0");
        member.setSort(0);
        return member;
    }

    private Project project(String status, Long managerEmployeeId) {
        Project project = new Project();
        project.setId(1L);
        project.setStatus(status);
        project.setManagerEmployeeId(managerEmployeeId);
        return project;
    }

    private EmployeeVo employee(String employmentStatus, String status) {
        EmployeeVo employee = new EmployeeVo();
        employee.setId(2L);
        employee.setEmploymentStatus(employmentStatus);
        employee.setStatus(status);
        return employee;
    }
}
