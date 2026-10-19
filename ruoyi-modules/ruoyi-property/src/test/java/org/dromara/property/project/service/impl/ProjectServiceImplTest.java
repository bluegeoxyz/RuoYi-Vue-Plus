package org.dromara.property.project.service.impl;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.service.DictService;
import org.dromara.hr.employee.domain.vo.EmployeeOptionVo;
import org.dromara.hr.employee.service.IEmployeeService;
import org.dromara.property.project.domain.Project;
import org.dromara.property.project.domain.bo.ProjectBo;
import org.dromara.property.project.domain.vo.ProjectOptionVo;
import org.dromara.property.project.domain.vo.ProjectVo;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.project.mapper.ProjectMemberMapper;
import org.dromara.property.scope.PropertyProjectScope;
import org.dromara.property.scope.PropertyProjectScopeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 物业项目服务单元测试。 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProjectServiceImplTest {

    @Mock
    private ProjectMapper projectMapper;
    @Mock
    private ProjectMemberMapper projectMemberMapper;
    @Mock
    private IEmployeeService employeeService;
    @Mock
    private DictService dictService;
    @Mock
    private PropertyProjectScopeService scopeService;
    @InjectMocks
    private ProjectServiceImpl service;

    @BeforeEach
    void setUp() {
        when(scopeService.resolveManagementScope(any())).thenReturn(PropertyProjectScope.all());
        when(dictService.getAllDictByDictType("pm_project_type")).thenReturn(Map.of("RESIDENTIAL", "住宅"));
        when(dictService.getAllDictByDictType("sys_normal_disable")).thenReturn(Map.of("0", "正常", "1", "停用"));
    }

    @Test
    void insertShouldNormalizeDefaultsAndSucceedWithoutManager() {
        ProjectBo bo = validBo();
        bo.setStatus(null);
        bo.setSort(null);
        doAnswer(invocation -> {
            Project entity = invocation.getArgument(0);
            entity.setId(10L);
            return 1;
        }).when(projectMapper).insert(any(Project.class));

        assertTrue(service.insertByBo(bo));
        assertEquals(10L, bo.getId());
        ArgumentCaptor<Project> captor = ArgumentCaptor.forClass(Project.class);
        verify(projectMapper).insert(captor.capture());
        assertEquals("P001", captor.getValue().getProjectCode());
        assertEquals("0", captor.getValue().getStatus());
        assertEquals(0, captor.getValue().getSort());
        verify(employeeService, never()).queryById(anyLong());
    }

    @Test
    void duplicateProjectCodeShouldFailForInsertAndAnotherProjectOnUpdate() {
        when(projectMapper.countByProjectCode("P001", null)).thenReturn(1L);
        assertThrows(ServiceException.class, () -> service.insertByBo(validBo()));

        ProjectBo bo = validBo();
        bo.setId(1L);
        when(projectMapper.selectById(1L)).thenReturn(new Project());
        when(projectMapper.countByProjectCode("P001", 1L)).thenReturn(1L);
        assertThrows(ServiceException.class, () -> service.updateByBo(bo));
    }

    @Test
    void updateShouldExcludeOwnProjectCode() {
        ProjectBo bo = validBo();
        bo.setId(1L);
        when(projectMapper.selectById(1L)).thenReturn(new Project());
        when(projectMapper.countByProjectCode("P001", 1L)).thenReturn(0L);
        when(projectMapper.updateById(any(Project.class))).thenReturn(1);
        assertTrue(service.updateByBo(bo));
        verify(projectMapper).countByProjectCode("P001", 1L);
    }

    @Test
    void managerMustBeMaintainedThroughProjectMemberAggregate() {
        ProjectBo bo = validBo();
        bo.setManagerEmployeeId(8L);
        ServiceException error = assertThrows(ServiceException.class, () -> service.insertByBo(bo));
        assertEquals("项目负责人只能通过项目成员管理设置", error.getMessage());
        verify(employeeService, never()).queryById(anyLong());
        verify(projectMapper, never()).insert(any(Project.class));
    }

    @Test
    void invalidCoordinateShouldFail() {
        ProjectBo bo = validBo();
        bo.setLongitude(new BigDecimal("180.0000001"));
        assertThrows(ServiceException.class, () -> service.insertByBo(bo));
        bo.setLongitude(null);
        bo.setLatitude(new BigDecimal("-90.0000001"));
        assertThrows(ServiceException.class, () -> service.insertByBo(bo));
    }

    @Test
    void statusShouldSupportEnableAndDisable() {
        when(projectMapper.selectById(1L)).thenReturn(new Project());
        when(projectMapper.updateById(any(Project.class))).thenReturn(1);
        assertTrue(service.changeStatus(1L, "1"));
        assertTrue(service.changeStatus(1L, "0"));
        verify(projectMapper, org.mockito.Mockito.times(2)).updateById(any(Project.class));
    }

    @Test
    void deleteShouldRejectMissingProjectAndDeleteExistingProjects() {
        when(projectMapper.selectByIds(any(java.util.Collection.class)))
            .thenReturn(List.of(new Project()))
            .thenReturn(List.of(new Project()));
        assertThrows(ServiceException.class, () -> service.deleteWithValidByIds(List.of(1L, 2L)));
        verify(projectMapper, never()).deleteByIds(any(java.util.Collection.class));

        when(projectMapper.deleteByIds(any(java.util.Collection.class))).thenReturn(1);
        assertTrue(service.deleteWithValidByIds(List.of(1L)));
    }

    @Test
    void projectWithMemberHistoryCannotBeDeleted() {
        when(projectMapper.selectByIds(any(java.util.Collection.class))).thenReturn(List.of(new Project()));
        when(projectMemberMapper.existsByProjectIds(any())).thenReturn(true);
        assertThrows(ServiceException.class, () -> service.deleteWithValidByIds(List.of(1L)));
        verify(projectMapper, never()).deleteByIds(any(java.util.Collection.class));
    }

    @Test
    void listShouldBatchEnrichManagersWithoutNPlusOne() {
        ProjectVo first = projectVo(1L, 8L);
        ProjectVo second = projectVo(2L, 8L);
        when(projectMapper.selectVoList(any())).thenReturn(List.of(first, second));
        EmployeeOptionVo manager = new EmployeeOptionVo();
        manager.setId(8L);
        manager.setEmployeeNo("E008");
        manager.setEmployeeName("王经理");
        when(employeeService.selectEmployeeOptionsByIds(List.of(8L))).thenReturn(List.of(manager));

        List<ProjectVo> result = service.queryList(new ProjectBo());
        assertEquals(List.of("王经理", "王经理"), result.stream().map(ProjectVo::getManagerEmployeeName).toList());
        verify(employeeService).selectEmployeeOptionsByIds(List.of(8L));
        verify(employeeService, never()).queryById(anyLong());
    }

    @Test
    void optionsShouldUseMapperEntry() {
        when(projectMapper.selectOptions()).thenReturn(List.of(new ProjectOptionVo()));
        assertEquals(1, service.selectOptions().size());
        verify(projectMapper).selectOptions();
    }

    private ProjectBo validBo() {
        ProjectBo bo = new ProjectBo();
        bo.setProjectCode(" P001 ");
        bo.setProjectName(" 一号项目 ");
        bo.setProjectType("RESIDENTIAL");
        bo.setStatus("0");
        return bo;
    }

    private ProjectVo projectVo(Long id, Long managerId) {
        ProjectVo project = new ProjectVo();
        project.setId(id);
        project.setManagerEmployeeId(managerId);
        return project;
    }
}
