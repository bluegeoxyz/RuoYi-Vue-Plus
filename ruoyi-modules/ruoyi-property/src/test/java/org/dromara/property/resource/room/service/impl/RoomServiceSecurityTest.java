package org.dromara.property.resource.room.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.customer.relation.mapper.RoomCustomerRelationMapper;
import org.dromara.property.resource.building.domain.Building;
import org.dromara.property.resource.building.mapper.BuildingMapper;
import org.dromara.property.resource.room.domain.Room;
import org.dromara.property.resource.room.domain.bo.RoomBo;
import org.dromara.property.resource.room.domain.vo.RoomVo;
import org.dromara.property.resource.room.mapper.RoomMapper;
import org.dromara.property.resource.unit.domain.BuildingUnit;
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
class RoomServiceSecurityTest {
    @Mock RoomMapper roomMapper; @Mock ProjectMapper projectMapper; @Mock BuildingMapper buildingMapper;
    @Mock BuildingUnitMapper unitMapper; @Mock RoomCustomerRelationMapper relationMapper;
    @Mock PropertyProjectScopeService scopeService;
    private RoomServiceImpl service;

    @BeforeEach void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), "room-test"), Room.class);
        service = new RoomServiceImpl(roomMapper, projectMapper, buildingMapper, unitMapper, scopeService, relationMapper);
    }
    @Test void noneScopeReturnsEmptyWithoutDatabaseQuery() {
        when(scopeService.resolveBusinessScope(any(), any())).thenReturn(PropertyProjectScope.none());
        assertEquals(0, service.queryPage(new RoomBo(), new PageQuery()).getTotal());
        assertTrue(service.queryExport(new RoomBo()).isEmpty()); assertTrue(service.options(new RoomBo()).isEmpty());
        verifyNoInteractions(roomMapper, projectMapper, buildingMapper, unitMapper);
    }
    @Test void detailAuthorizesPersistedProject() {
        when(roomMapper.selectById(1L)).thenReturn(room(1L, 101L, 11L, 21L));
        doThrow(new ServiceException("拒绝")).when(scopeService).assertBusinessAccess(101L, PropertyBusinessDomain.RESOURCE, ProjectBusinessAction.READ);
        assertThrows(ServiceException.class, () -> service.queryById(1L)); verify(roomMapper, never()).selectVoById(anyLong());
    }
    @Test void insertRejectsForgedBuildingOrUnitRelationship() {
        RoomBo bo = bo(null, 101L, 11L, 21L, "R1");
        var project = new org.dromara.property.project.domain.Project(); project.setId(101L); project.setStatus("0");
        Building building = new Building(); building.setId(11L); building.setProjectId(101L); building.setStatus("0");
        BuildingUnit unit = new BuildingUnit(); unit.setId(21L); unit.setProjectId(101L); unit.setBuildingId(99L); unit.setStatus("0");
        when(projectMapper.selectById(101L)).thenReturn(project); when(buildingMapper.selectActiveByIdForUpdate(11L)).thenReturn(building); when(unitMapper.selectActiveByIdForUpdate(21L)).thenReturn(unit);
        assertThrows(ServiceException.class, () -> service.insert(bo)); verify(roomMapper, never()).insert(any(Room.class));
    }
    @Test void updateRejectsForgedProjectBuildingOrUnit() {
        when(roomMapper.selectActiveByIdForUpdate(1L)).thenReturn(room(1L, 101L, 11L, 21L));
        assertThrows(ServiceException.class, () -> service.update(bo(1L, 202L, 11L, 21L, "R1")));
        assertThrows(ServiceException.class, () -> service.update(bo(1L, 101L, 12L, 21L, "R1")));
        assertThrows(ServiceException.class, () -> service.update(bo(1L, 101L, 11L, 22L, "R1")));
        verify(roomMapper, never()).updateById(any(Room.class));
    }
    @Test void duplicateCodeIsRejectedBeforeInsert() {
        RoomBo bo = bo(null, 101L, 11L, 21L, "R1");
        var project = new org.dromara.property.project.domain.Project(); project.setId(101L); project.setStatus("0");
        Building building = new Building(); building.setId(11L); building.setProjectId(101L); building.setStatus("0");
        BuildingUnit unit = new BuildingUnit(); unit.setId(21L); unit.setProjectId(101L); unit.setBuildingId(11L); unit.setStatus("0");
        when(projectMapper.selectById(101L)).thenReturn(project); when(buildingMapper.selectActiveByIdForUpdate(11L)).thenReturn(building); when(unitMapper.selectActiveByIdForUpdate(21L)).thenReturn(unit); when(roomMapper.countActiveCode(101L, "R1", null)).thenReturn(1L);
        assertThrows(ServiceException.class, () -> service.insert(bo)); verify(roomMapper, never()).insert(any(Room.class));
    }
    @Test void customerRelationPreventsDelete() {
        when(roomMapper.selectActiveByIdForUpdate(1L)).thenReturn(room(1L, 101L, 11L, 21L)); when(roomMapper.selectEffectiveCustomerRelationIdsForUpdate(1L)).thenReturn(List.of(9L));
        assertThrows(ServiceException.class, () -> service.delete(List.of(1L))); verify(roomMapper, never()).deleteByIds(anyCollection());
    }
    @Test void mixedScopeBatchDeleteFailsBeforeDelete() {
        when(roomMapper.selectActiveByIdForUpdate(1L)).thenReturn(room(1L, 101L, 11L, 21L)); when(roomMapper.selectActiveByIdForUpdate(2L)).thenReturn(room(2L, 202L, 12L, 22L));
        doAnswer(i -> { if (Long.valueOf(202L).equals(i.getArgument(0))) throw new ServiceException("拒绝"); return null; }).when(scopeService).assertBusinessAccess(anyLong(), eq(PropertyBusinessDomain.RESOURCE), eq(ProjectBusinessAction.WRITE));
        assertThrows(ServiceException.class, () -> service.delete(List.of(1L, 2L))); verify(roomMapper, never()).deleteByIds(anyCollection());
    }
    @Test @SuppressWarnings("unchecked") void listExportAndOptionsUseIndependentScopes() {
        when(scopeService.resolveBusinessScope(any(), any())).thenReturn(PropertyProjectScope.explicit(Set.of(101L)));
        when(roomMapper.selectVoPage(any(), any())).thenReturn(new Page<RoomVo>()); when(roomMapper.selectVoList(any())).thenReturn(List.of());
        service.queryPage(new RoomBo(), new PageQuery()); service.queryExport(new RoomBo()); service.options(new RoomBo());
        verify(scopeService).resolveBusinessScope(PropertyBusinessDomain.RESOURCE, ProjectBusinessAction.READ);
        verify(scopeService).resolveBusinessScope(PropertyBusinessDomain.RESOURCE, ProjectBusinessAction.EXPORT);
        verify(scopeService).resolveBusinessScope(PropertyBusinessDomain.RESOURCE, ProjectBusinessAction.OPTION);
    }
    private static Room room(Long id, Long projectId, Long buildingId, Long unitId) { Room r=new Room();r.setId(id);r.setProjectId(projectId);r.setBuildingId(buildingId);r.setUnitId(unitId);r.setRoomCode("R"+id);return r; }
    private static RoomBo bo(Long id, Long projectId, Long buildingId, Long unitId, String code) { RoomBo b=new RoomBo();b.setId(id);b.setProjectId(projectId);b.setBuildingId(buildingId);b.setUnitId(unitId);b.setRoomCode(code);b.setRoomNo(code);return b; }
}
