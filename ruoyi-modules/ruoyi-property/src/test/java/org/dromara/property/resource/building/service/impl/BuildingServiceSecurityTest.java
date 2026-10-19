package org.dromara.property.resource.building.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.property.project.domain.vo.ProjectOptionVo;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.resource.building.domain.Building;
import org.dromara.property.resource.building.domain.bo.BuildingBo;
import org.dromara.property.resource.building.domain.vo.BuildingVo;
import org.dromara.property.resource.building.mapper.BuildingMapper;
import org.dromara.property.scope.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuildingServiceSecurityTest {
    @Mock private BuildingMapper buildingMapper;
    @Mock private ProjectMapper projectMapper;
    @Mock private PropertyProjectScopeService scopeService;
    private BuildingServiceImpl service;

    @BeforeEach void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), "building-test"), Building.class);
        service = new BuildingServiceImpl(buildingMapper, projectMapper, scopeService);
    }

    @Test void noneScopeMustReturnEmptyInsteadOfQueryingAllRows() {
        when(scopeService.resolveBusinessScope(any(), any())).thenReturn(PropertyProjectScope.none());
        assertEquals(0, service.queryPage(new BuildingBo(), new PageQuery()).getTotal());
        assertTrue(service.queryExport(new BuildingBo()).isEmpty());
        assertTrue(service.options(new BuildingBo()).isEmpty());
        assertTrue(service.projectOptions().isEmpty());
        verifyNoInteractions(buildingMapper, projectMapper);
    }

    @Test void detailMustAuthorizePersistedProject() {
        when(buildingMapper.selectById(10L)).thenReturn(building(10L, 101L));
        doThrow(new ServiceException("无项目业务访问权限")).when(scopeService)
            .assertBusinessAccess(101L, PropertyBusinessDomain.RESOURCE, ProjectBusinessAction.READ);
        assertThrows(ServiceException.class, () -> service.queryById(10L));
        verify(buildingMapper, never()).selectVoById(anyLong());
    }

    @Test void updateMustRejectForgedProjectId() {
        when(buildingMapper.selectById(10L)).thenReturn(building(10L, 101L));
        BuildingBo bo = new BuildingBo();
        bo.setId(10L); bo.setProjectId(202L); bo.setBuildingCode("B01");
        ServiceException error = assertThrows(ServiceException.class, () -> service.update(bo));
        assertEquals("楼栋不允许跨项目迁移", error.getMessage());
        verify(scopeService).assertBusinessAccess(101L, PropertyBusinessDomain.RESOURCE, ProjectBusinessAction.WRITE);
        verify(buildingMapper, never()).updateById(any(Building.class));
    }

    @Test void statusChangeMustAuthorizePersistedProject() {
        when(buildingMapper.selectById(10L)).thenReturn(building(10L, 101L));
        doThrow(new ServiceException("无项目业务访问权限")).when(scopeService)
            .assertBusinessAccess(101L, PropertyBusinessDomain.RESOURCE, ProjectBusinessAction.WRITE);
        assertThrows(ServiceException.class, () -> service.changeStatus(10L, "1"));
        verify(buildingMapper, never()).updateById(any(Building.class));
    }

    @Test void mixedScopeBatchDeleteMustFailBeforeDelete() {
        when(buildingMapper.selectActiveByIdForUpdate(10L)).thenReturn(building(10L, 101L));
        when(buildingMapper.selectActiveByIdForUpdate(20L)).thenReturn(building(20L, 202L));
        doAnswer(invocation -> {
            if (Long.valueOf(202L).equals(invocation.getArgument(0))) throw new ServiceException("无项目业务访问权限");
            return null;
        }).when(scopeService).assertBusinessAccess(anyLong(), eq(PropertyBusinessDomain.RESOURCE), eq(ProjectBusinessAction.WRITE));
        assertThrows(ServiceException.class, () -> service.delete(List.of(10L, 20L)));
        verify(buildingMapper, never()).deleteByIds(anyCollection());
    }

    @Test void deleteMustRejectActiveUnitOrRoom() {
        when(buildingMapper.selectActiveByIdForUpdate(10L)).thenReturn(building(10L, 101L));
        when(buildingMapper.existsActiveChildren(10L)).thenReturn(true);
        ServiceException error = assertThrows(ServiceException.class, () -> service.delete(List.of(10L)));
        assertTrue(error.getMessage().contains("单元或房屋"));
        verify(buildingMapper, never()).deleteByIds(anyCollection());
    }

    @Test @SuppressWarnings("unchecked") void listAndExportMustResolveDifferentResourceActions() {
        PropertyProjectScope scope = PropertyProjectScope.explicit(Set.of(101L));
        when(scopeService.resolveBusinessScope(PropertyBusinessDomain.RESOURCE, ProjectBusinessAction.READ)).thenReturn(scope);
        when(scopeService.resolveBusinessScope(PropertyBusinessDomain.RESOURCE, ProjectBusinessAction.EXPORT)).thenReturn(scope);
        when(buildingMapper.selectVoPage(any(), any())).thenReturn(new Page<BuildingVo>());
        when(buildingMapper.selectVoList(any())).thenReturn(List.of());
        service.queryPage(new BuildingBo(), new PageQuery());
        service.queryExport(new BuildingBo());
        verify(scopeService).resolveBusinessScope(PropertyBusinessDomain.RESOURCE, ProjectBusinessAction.READ);
        verify(scopeService).resolveBusinessScope(PropertyBusinessDomain.RESOURCE, ProjectBusinessAction.EXPORT);
    }

    @Test void resourceProjectOptionsMustNotUseManagementScope() {
        when(scopeService.resolveBusinessScope(PropertyBusinessDomain.RESOURCE, ProjectBusinessAction.OPTION)).thenReturn(PropertyProjectScope.all());
        ProjectOptionVo option = new ProjectOptionVo(); option.setId(101L);
        when(projectMapper.selectOptions()).thenReturn(List.of(option));
        assertEquals(List.of(option), service.projectOptions());
        verify(scopeService, never()).resolveManagementScope(any());
    }

    private static Building building(Long id, Long projectId) {
        Building building = new Building(); building.setId(id); building.setProjectId(projectId); building.setBuildingCode("B" + id); return building;
    }
}
