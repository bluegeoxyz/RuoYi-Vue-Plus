package org.dromara.property.resource.importer.service.impl;

import org.dromara.common.excel.importer.ImportResult;
import org.dromara.property.dict.PropertyDictionaryService;
import org.dromara.property.dict.PropertyDictTypes;
import org.dromara.property.project.domain.Project;
import org.dromara.property.project.mapper.ProjectMapper;
import org.dromara.property.resource.building.mapper.BuildingMapper;
import org.dromara.property.resource.importer.domain.PropertyResourceImportRow;
import org.dromara.property.resource.room.mapper.RoomMapper;
import org.dromara.property.resource.unit.mapper.BuildingUnitMapper;
import org.dromara.property.scope.PropertyProjectScopeService;
import org.dromara.property.scope.PropertyFunctionalPermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PropertyResourceImportServiceImplTest {
    private ProjectMapper projectMapper;
    private PropertyDictionaryService dictionaryService;
    private PropertyResourceImportServiceImpl service;

    @BeforeEach
    void setUp() {
        projectMapper = mock(ProjectMapper.class);
        BuildingMapper buildingMapper = mock(BuildingMapper.class);
        BuildingUnitMapper unitMapper = mock(BuildingUnitMapper.class);
        RoomMapper roomMapper = mock(RoomMapper.class);
        PropertyProjectScopeService scopeService = mock(PropertyProjectScopeService.class);
        TransactionTemplate tx = mock(TransactionTemplate.class);
        dictionaryService = mock(PropertyDictionaryService.class);
        when(dictionaryService.resolveInput(anyString(), anyString())).thenAnswer(invocation -> invocation.getArgument(1));
        Project project = new Project(); project.setId(1L); project.setStatus("0");
        when(projectMapper.selectById(1L)).thenReturn(project);
        when(buildingMapper.selectList(any())).thenReturn(List.of());
        when(unitMapper.selectList(any())).thenReturn(List.of());
        when(roomMapper.selectList(any())).thenReturn(List.of());
        service = new PropertyResourceImportServiceImpl(projectMapper, buildingMapper, unitMapper, roomMapper, scopeService,
            mock(PropertyFunctionalPermissionService.class), dictionaryService, tx, List.of());
    }

    @Test
    void chineseLabelsAreConvertedToStableCodes() {
        PropertyResourceImportRow row = row("1#", "1号楼", "1", "A-101");
        row.setBuildingType("住宅"); row.setRoomType("住宅"); row.setPropertyType("私有产权");
        row.setDeliveryStatus("已交付"); row.setOccupancyStatus("空置"); row.setStatus("正常");
        when(dictionaryService.resolveInput(PropertyDictTypes.BUILDING_TYPE, "住宅")).thenReturn("RESIDENTIAL");
        when(dictionaryService.resolveInput(PropertyDictTypes.ROOM_TYPE, "住宅")).thenReturn("RESIDENCE");
        when(dictionaryService.resolveInput(PropertyDictTypes.PROPERTY_TYPE, "私有产权")).thenReturn("PRIVATE");
        when(dictionaryService.resolveInput(PropertyDictTypes.DELIVERY_STATUS, "已交付")).thenReturn("DELIVERED");
        when(dictionaryService.resolveInput(PropertyDictTypes.OCCUPANCY_STATUS, "空置")).thenReturn("VACANT");
        when(dictionaryService.resolveInput(PropertyDictTypes.NORMAL_DISABLE, "正常")).thenReturn("0");
        ImportResult result = service.importRows(1L, List.of(row));
        assertEquals(1, result.getSuccessRows());
        assertEquals("RESIDENTIAL", row.getBuildingType());
        assertEquals("DELIVERED", row.getDeliveryStatus());
        assertEquals("VACANT", row.getOccupancyStatus());
    }

    @Test
    void invalidChineseLabelRejectsWholeImport() {
        PropertyResourceImportRow row = row("1#", "1号楼", "1", "A-101");
        row.setDeliveryStatus("已交房");
        when(dictionaryService.resolveInput(PropertyDictTypes.DELIVERY_STATUS, "已交房")).thenReturn(null);
        ImportResult result = service.importRows(1L, List.of(row));
        assertEquals(0, result.getSuccessRows());
        assertEquals(1, result.getFailedRows());
        assertTrue(result.getErrors().stream().anyMatch(error -> error.getMessage().equals("交付状态“已交房”不是有效值")));
    }

    @Test
    void occupiedRoomStatusIsRejectedBecauseOccupancyIsRelationDerived() {
        PropertyResourceImportRow row = row("1#", "1号楼", "1", "A-101");
        row.setOccupancyStatus("已入住");
        when(dictionaryService.resolveInput(PropertyDictTypes.OCCUPANCY_STATUS, "已入住")).thenReturn("OCCUPIED");
        ImportResult result = service.importRows(1L, List.of(row));
        assertEquals(0, result.getSuccessRows());
        assertEquals(1, result.getFailedRows());
        assertTrue(result.getErrors().stream().anyMatch(e -> "occupancyStatus".equals(e.getField())
            && e.getMessage().contains("只能为空置")));
    }

    @Test
    void duplicateRoomMakesWholeFileFail() {
        PropertyResourceImportRow first = row("1#", "1号楼", "1", "A-101");
        PropertyResourceImportRow second = row("1#", "1号楼", "1", "A-101");
        ImportResult result = service.importRows(1L, List.of(first, second));
        assertEquals(0, result.getSuccessRows());
        assertEquals(2, result.getFailedRows());
        assertTrue(result.getErrors().stream().anyMatch(e -> "roomCode".equals(e.getField())));
    }

    @Test
    void inconsistentBuildingMakesWholeFileFailAndReportsBothRows() {
        PropertyResourceImportRow first = row("1#", "1号楼", "1", "A-101");
        PropertyResourceImportRow second = row("1#", "一号楼", "2", "A-201");
        ImportResult result = service.importRows(1L, List.of(first, second));
        assertEquals(0, result.getSuccessRows());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.getMessage().contains("第 2 行和第 3 行")));
    }

    @Test
    void overLimitRejectsEveryRowBeforeDatabaseValidation() {
        List<PropertyResourceImportRow> rows = new ArrayList<>();
        for (int i = 0; i < 2001; i++) rows.add(row("B" + i, "楼栋" + i, "1", "R" + i));
        ImportResult result = service.importRows(1L, rows);
        assertEquals(2001, result.getFailedRows());
        assertTrue(result.getErrors().getFirst().getMessage().contains("单次最多导入 2000 条"));
        verify(projectMapper, never()).selectById(anyLong());
    }

    private PropertyResourceImportRow row(String buildingCode, String buildingName, String unitCode, String roomCode) {
        PropertyResourceImportRow row = new PropertyResourceImportRow();
        row.setBuildingCode(buildingCode); row.setBuildingName(buildingName); row.setBuildingType("RESIDENTIAL");
        row.setUnitCode(unitCode); row.setUnitName(unitCode + "单元"); row.setRoomCode(roomCode); row.setRoomNo(roomCode);
        row.setRoomType("RESIDENCE"); row.setPropertyType("PRIVATE"); row.setDeliveryStatus("UNDELIVERED");
        row.setOccupancyStatus("VACANT"); row.setStatus("0");
        return row;
    }
}
