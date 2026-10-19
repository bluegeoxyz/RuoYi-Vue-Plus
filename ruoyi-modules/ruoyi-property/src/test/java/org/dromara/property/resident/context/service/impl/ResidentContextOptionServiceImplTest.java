package org.dromara.property.resident.context.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.security.business.BusinessContext;
import org.dromara.common.security.business.BusinessContextAccessService;
import org.dromara.common.security.business.ReferenceType;
import org.dromara.property.project.domain.Project;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.resource.building.domain.Building;
import org.dromara.property.resource.building.mapper.BuildingMapper;
import org.dromara.property.resource.room.mapper.RoomMapper;
import org.dromara.property.resource.unit.domain.BuildingUnit;
import org.dromara.property.resource.unit.mapper.BuildingUnitMapper;
import org.dromara.property.scope.ProjectBusinessAction;
import org.dromara.property.scope.PropertyBusinessDomain;
import org.dromara.property.scope.PropertyProjectScopeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResidentContextOptionServiceImplTest {

    @Mock BusinessContextAccessService contextAccessService;
    @Mock PropertyProjectScopeService projectScopeService;
    @Mock ProjectMapper projectMapper;
    @Mock BuildingMapper buildingMapper;
    @Mock BuildingUnitMapper unitMapper;
    @Mock RoomMapper roomMapper;
    ResidentContextOptionServiceImpl service;

    @BeforeEach
    void setUp() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "resident-context-test");
        TableInfoHelper.initTableInfo(assistant, Building.class);
        TableInfoHelper.initTableInfo(assistant, BuildingUnit.class);
        TableInfoHelper.initTableInfo(assistant, org.dromara.property.resource.room.domain.Room.class);
        service = new ResidentContextOptionServiceImpl(contextAccessService, projectScopeService,
            projectMapper, buildingMapper, unitMapper, roomMapper);
    }

    @Test
    void buildingsRequireResidentContextAndCustomerRelationProjectScope() {
        Project project = new Project();
        project.setId(1L);
        project.setStatus("0");
        Building building = new Building();
        building.setId(10L);
        building.setProjectId(1L);
        building.setBuildingCode("B01");
        building.setBuildingName("一号楼");
        when(projectMapper.selectById(1L)).thenReturn(project);
        when(buildingMapper.selectList(any())).thenReturn(List.of(building));

        var result = service.buildings(1L);

        assertEquals(1, result.size());
        assertEquals("一号楼", result.getFirst().buildingName());
        verify(contextAccessService).requireAccess(BusinessContext.RESIDENT_QUERY, ReferenceType.BUILDING);
        verify(projectScopeService).assertBusinessAccess(1L, PropertyBusinessDomain.CUSTOMER_RELATION,
            ProjectBusinessAction.OPTION);
    }

    @Test
    void crossProjectBuildingCannotExposeUnits() {
        Project project = new Project();
        project.setId(1L);
        project.setStatus("0");
        Building building = new Building();
        building.setId(10L);
        building.setProjectId(2L);
        building.setStatus("0");
        when(projectMapper.selectById(1L)).thenReturn(project);
        when(buildingMapper.selectById(10L)).thenReturn(building);

        assertThrows(ServiceException.class, () -> service.units(1L, 10L));

        verify(contextAccessService).requireAccess(BusinessContext.RESIDENT_QUERY, ReferenceType.UNIT);
        verifyNoInteractions(unitMapper);
    }

    @Test
    void crossBuildingUnitCannotExposeRooms() {
        Project project = new Project();
        project.setId(1L);
        project.setStatus("0");
        Building building = new Building();
        building.setId(10L);
        building.setProjectId(1L);
        building.setStatus("0");
        BuildingUnit unit = new BuildingUnit();
        unit.setId(20L);
        unit.setProjectId(1L);
        unit.setBuildingId(11L);
        unit.setStatus("0");
        when(projectMapper.selectById(1L)).thenReturn(project);
        when(buildingMapper.selectById(10L)).thenReturn(building);
        when(unitMapper.selectById(20L)).thenReturn(unit);

        assertThrows(ServiceException.class, () -> service.rooms(1L, 10L, 20L));

        verify(contextAccessService).requireAccess(BusinessContext.RESIDENT_QUERY, ReferenceType.ROOM);
        verifyNoInteractions(roomMapper);
    }
}
