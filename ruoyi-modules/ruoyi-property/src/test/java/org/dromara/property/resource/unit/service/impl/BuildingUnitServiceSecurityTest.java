package org.dromara.property.resource.unit.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.property.project.domain.Project;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.resource.building.domain.Building;
import org.dromara.property.resource.building.mapper.BuildingMapper;
import org.dromara.property.resource.unit.domain.BuildingUnit;
import org.dromara.property.resource.unit.domain.bo.BuildingUnitBo;
import org.dromara.property.resource.unit.domain.vo.BuildingUnitVo;
import org.dromara.property.resource.unit.mapper.BuildingUnitMapper;
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
class BuildingUnitServiceSecurityTest {
    @Mock private BuildingUnitMapper unitMapper;
    @Mock private BuildingMapper buildingMapper;
    @Mock private ProjectMapper projectMapper;
    @Mock private PropertyProjectScopeService scopeService;
    private BuildingUnitServiceImpl service;

    @BeforeEach void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), "unit-test"), BuildingUnit.class);
        service = new BuildingUnitServiceImpl(unitMapper, buildingMapper, projectMapper, scopeService);
    }

    @Test void noneScopeReturnsEmptyWithoutDatabaseQuery() {
        when(scopeService.resolveBusinessScope(any(), any())).thenReturn(PropertyProjectScope.none());
        assertEquals(0, service.queryPage(new BuildingUnitBo(), new PageQuery()).getTotal());
        assertTrue(service.queryExport(new BuildingUnitBo()).isEmpty());
        assertTrue(service.options(new BuildingUnitBo()).isEmpty());
        verifyNoInteractions(unitMapper, buildingMapper, projectMapper);
    }

    @Test void detailAuthorizesPersistedProject() {
        when(unitMapper.selectById(10L)).thenReturn(unit(10L, 101L, 1001L));
        doThrow(new ServiceException("无项目业务访问权限")).when(scopeService)
            .assertBusinessAccess(101L, PropertyBusinessDomain.RESOURCE, ProjectBusinessAction.READ);
        assertThrows(ServiceException.class, () -> service.queryById(10L));
        verify(unitMapper, never()).selectVoById(anyLong());
    }

    @Test void insertRejectsBuildingFromAnotherProject() {
        BuildingUnitBo bo = bo(null, 101L, 1001L, "U1");
        Project project = new Project(); project.setId(101L); project.setStatus("0");
        Building building = new Building(); building.setId(1001L); building.setProjectId(202L); building.setStatus("0");
        when(projectMapper.selectById(101L)).thenReturn(project);
        when(buildingMapper.selectActiveByIdForUpdate(1001L)).thenReturn(building);
        assertEquals("楼栋不属于目标项目", assertThrows(ServiceException.class, () -> service.insert(bo)).getMessage());
        verify(unitMapper, never()).insert(any(BuildingUnit.class));
    }

    @Test void updateRejectsForgedProjectOrBuilding() {
        when(unitMapper.selectById(10L)).thenReturn(unit(10L, 101L, 1001L));
        BuildingUnitBo bo = bo(10L, 101L, 2002L, "U1");
        assertTrue(assertThrows(ServiceException.class, () -> service.update(bo)).getMessage().contains("所属楼栋"));
        verify(scopeService).assertBusinessAccess(101L, PropertyBusinessDomain.RESOURCE, ProjectBusinessAction.WRITE);
        verify(unitMapper, never()).updateById(any(BuildingUnit.class));
    }

    @Test void duplicateCodeIsRejectedBeforeInsert() {
        BuildingUnitBo bo = bo(null, 101L, 1001L, "U1");
        Project project = new Project(); project.setId(101L); project.setStatus("0");
        Building building = new Building(); building.setId(1001L); building.setProjectId(101L); building.setStatus("0");
        when(projectMapper.selectById(101L)).thenReturn(project);
        when(buildingMapper.selectActiveByIdForUpdate(1001L)).thenReturn(building);
        when(unitMapper.countActiveCode(1001L, "U1", null)).thenReturn(1L);
        assertThrows(ServiceException.class, () -> service.insert(bo));
        verify(unitMapper, never()).insert(any(BuildingUnit.class));
    }

    @Test void mixedScopeBatchDeleteFailsBeforeDelete() {
        when(unitMapper.selectActiveByIdForUpdate(10L)).thenReturn(unit(10L, 101L, 1001L));
        when(unitMapper.selectActiveByIdForUpdate(20L)).thenReturn(unit(20L, 202L, 2001L));
        when(unitMapper.selectById(10L)).thenReturn(unit(10L, 101L, 1001L));
        when(unitMapper.selectById(20L)).thenReturn(unit(20L, 202L, 2001L));
        Building firstBuilding = new Building(); firstBuilding.setId(1001L); firstBuilding.setProjectId(101L);
        Building secondBuilding = new Building(); secondBuilding.setId(2001L); secondBuilding.setProjectId(202L);
        when(buildingMapper.selectActiveByIdForUpdate(1001L)).thenReturn(firstBuilding);
        when(buildingMapper.selectActiveByIdForUpdate(2001L)).thenReturn(secondBuilding);
        doAnswer(invocation -> { if (Long.valueOf(202L).equals(invocation.getArgument(0))) throw new ServiceException("无项目业务访问权限"); return null; })
            .when(scopeService).assertBusinessAccess(anyLong(), eq(PropertyBusinessDomain.RESOURCE), eq(ProjectBusinessAction.WRITE));
        assertThrows(ServiceException.class, () -> service.delete(List.of(10L, 20L)));
        verify(unitMapper, never()).deleteByIds(anyCollection());
    }

    @Test void deleteRejectsActiveRoom() {
        when(unitMapper.selectActiveByIdForUpdate(10L)).thenReturn(unit(10L, 101L, 1001L));
        when(unitMapper.selectById(10L)).thenReturn(unit(10L, 101L, 1001L));
        Building building = new Building(); building.setId(1001L); building.setProjectId(101L);
        when(buildingMapper.selectActiveByIdForUpdate(1001L)).thenReturn(building);
        when(unitMapper.existsActiveRoom(10L)).thenReturn(true);
        assertTrue(assertThrows(ServiceException.class, () -> service.delete(List.of(10L))).getMessage().contains("房屋"));
        verify(unitMapper, never()).deleteByIds(anyCollection());
    }

    @Test @SuppressWarnings("unchecked") void readExportAndOptionsResolveSeparateActions() {
        PropertyProjectScope scope = PropertyProjectScope.explicit(Set.of(101L));
        when(scopeService.resolveBusinessScope(any(), any())).thenReturn(scope);
        when(unitMapper.selectVoPage(any(), any())).thenReturn(new Page<BuildingUnitVo>());
        when(unitMapper.selectVoList(any())).thenReturn(List.of());
        service.queryPage(new BuildingUnitBo(), new PageQuery());
        service.queryExport(new BuildingUnitBo());
        service.options(new BuildingUnitBo());
        verify(scopeService).resolveBusinessScope(PropertyBusinessDomain.RESOURCE, ProjectBusinessAction.READ);
        verify(scopeService).resolveBusinessScope(PropertyBusinessDomain.RESOURCE, ProjectBusinessAction.EXPORT);
        verify(scopeService).resolveBusinessScope(PropertyBusinessDomain.RESOURCE, ProjectBusinessAction.OPTION);
    }

    private static BuildingUnit unit(Long id, Long projectId, Long buildingId) {
        BuildingUnit unit = new BuildingUnit();
        unit.setId(id); unit.setProjectId(projectId); unit.setBuildingId(buildingId); unit.setUnitCode("U" + id);
        return unit;
    }

    private static BuildingUnitBo bo(Long id, Long projectId, Long buildingId, String code) {
        BuildingUnitBo bo = new BuildingUnitBo();
        bo.setId(id); bo.setProjectId(projectId); bo.setBuildingId(buildingId); bo.setUnitCode(code); bo.setUnitName(code);
        return bo;
    }
}
