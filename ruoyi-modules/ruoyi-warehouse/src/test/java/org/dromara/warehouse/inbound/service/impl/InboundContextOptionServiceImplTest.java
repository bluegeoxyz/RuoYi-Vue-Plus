package org.dromara.warehouse.inbound.service.impl;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.security.business.BusinessContext;
import org.dromara.common.security.business.BusinessContextAccessService;
import org.dromara.common.security.business.ReferenceType;
import org.dromara.warehouse.location.domain.vo.WarehouseLocationOptionVo;
import org.dromara.warehouse.location.mapper.WarehouseLocationMapper;
import org.dromara.warehouse.material.domain.vo.MaterialOptionVo;
import org.dromara.warehouse.material.mapper.MaterialMapper;
import org.dromara.warehouse.security.WarehouseAccessService;
import org.dromara.warehouse.unit.domain.vo.UnitOptionVo;
import org.dromara.warehouse.unit.mapper.UnitMapper;
import org.dromara.warehouse.warehouse.domain.Warehouse;
import org.dromara.warehouse.warehouse.mapper.WarehouseMapper;
import org.dromara.warehouse.zone.domain.vo.WarehouseZoneOptionVo;
import org.dromara.warehouse.zone.mapper.WarehouseZoneMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InboundContextOptionServiceImplTest {

    @Mock BusinessContextAccessService contextAccessService;
    @Mock WarehouseAccessService warehouseAccessService;
    @Mock WarehouseMapper warehouseMapper;
    @Mock WarehouseZoneMapper zoneMapper;
    @Mock WarehouseLocationMapper locationMapper;
    @Mock MaterialMapper materialMapper;
    @Mock UnitMapper unitMapper;
    @InjectMocks InboundContextOptionServiceImpl service;

    @Test
    void noAuthorizedWarehouseReturnsEmptyWithoutQueryingOptions() {
        when(warehouseAccessService.accessibleWarehouseIds()).thenReturn(List.of());

        assertEquals(List.of(), service.selectWarehouses());

        verify(contextAccessService).requireAccess(BusinessContext.INBOUND_MAINTAIN, ReferenceType.WAREHOUSE);
        verifyNoInteractions(warehouseMapper);
    }

    @Test
    void projectWarehouseScopeDenialStopsZoneLookup() {
        doThrow(new ServiceException("无项目仓库访问权限")).when(warehouseAccessService).requireWarehouse(10L);

        assertThrows(ServiceException.class, () -> service.selectZones(10L));

        verify(contextAccessService).requireAccess(BusinessContext.INBOUND_MAINTAIN, ReferenceType.ZONE);
        verifyNoInteractions(zoneMapper);
    }

    @Test
    void companyWarehouseDepartmentDenialStopsLocationLookup() {
        doThrow(new ServiceException("无部门数据权限")).when(warehouseAccessService).requireWarehouse(10L);

        assertThrows(ServiceException.class, () -> service.selectLocations(10L, null));

        verify(contextAccessService).requireAccess(BusinessContext.INBOUND_MAINTAIN, ReferenceType.LOCATION);
        verifyNoInteractions(zoneMapper, locationMapper);
    }

    @Test
    void rejectsZoneFromAnotherWarehouse() {
        when(warehouseAccessService.requireWarehouse(10L)).thenReturn(warehouse(10L, 100L));
        when(zoneMapper.selectOptions(10L)).thenReturn(List.of(zone(20L, 10L, "A区")));

        assertThrows(ServiceException.class, () -> service.selectLocations(10L, 21L));

        verify(locationMapper, never()).selectOptions(10L, 21L);
    }

    @Test
    void filtersLocationsWhoseZoneIsNotEnabledInAuthorizedWarehouse() {
        when(warehouseAccessService.requireWarehouse(10L)).thenReturn(warehouse(10L, 100L));
        when(zoneMapper.selectOptions(10L)).thenReturn(List.of(zone(20L, 10L, "A区")));
        when(locationMapper.selectOptions(10L, null)).thenReturn(List.of(
            location(30L, 10L, 20L), location(31L, 10L, 21L)));

        var result = service.selectLocations(10L, null);

        assertEquals(1, result.size());
        assertEquals(30L, result.getFirst().id());
        assertEquals("A区", result.getFirst().zoneName());
    }

    @Test
    void materialLookupUsesAuthorizedWarehouseCompanyAndEnabledUnits() {
        when(warehouseAccessService.requireWarehouse(10L)).thenReturn(warehouse(10L, 100L));
        when(materialMapper.selectOptions(Set.of(100L), 100L)).thenReturn(List.of(
            material(40L, 100L, 50L), material(41L, 100L, 51L)));
        when(unitMapper.selectOptions()).thenReturn(List.of(unit(50L, "件", 0)));

        var result = service.selectMaterials(10L);

        assertEquals(1, result.size());
        assertEquals(40L, result.getFirst().id());
        assertEquals("件", result.getFirst().unitName());
        verify(warehouseAccessService).assertMaterialCompanyAccess(100L);
        verify(contextAccessService).requireAccess(BusinessContext.INBOUND_MAINTAIN, ReferenceType.MATERIAL);
        verify(contextAccessService).requireAccess(BusinessContext.INBOUND_MAINTAIN, ReferenceType.MEASUREMENT_UNIT);
    }

    @Test
    void contextDenialStopsResourceLookup() {
        doThrow(new cn.dev33.satoken.exception.NotPermissionException("拒绝"))
            .when(contextAccessService).requireAccess(BusinessContext.INBOUND_MAINTAIN, ReferenceType.MATERIAL);

        assertThrows(cn.dev33.satoken.exception.NotPermissionException.class,
            () -> service.selectMaterials(10L));

        verifyNoInteractions(warehouseAccessService, materialMapper, unitMapper);
    }

    private static Warehouse warehouse(Long id, Long companyId) {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(id);
        warehouse.setCompanyId(companyId);
        return warehouse;
    }

    private static WarehouseZoneOptionVo zone(Long id, Long warehouseId, String name) {
        WarehouseZoneOptionVo zone = new WarehouseZoneOptionVo();
        zone.setId(id); zone.setWarehouseId(warehouseId); zone.setZoneCode("Z" + id); zone.setZoneName(name);
        return zone;
    }

    private static WarehouseLocationOptionVo location(Long id, Long warehouseId, Long zoneId) {
        WarehouseLocationOptionVo location = new WarehouseLocationOptionVo();
        location.setId(id); location.setWarehouseId(warehouseId); location.setZoneId(zoneId);
        location.setLocationCode("L" + id); location.setLocationName("库位" + id);
        return location;
    }

    private static MaterialOptionVo material(Long id, Long companyId, Long unitId) {
        MaterialOptionVo material = new MaterialOptionVo();
        material.setId(id); material.setCompanyId(companyId); material.setMaterialCode("M" + id);
        material.setMaterialName("物料" + id); material.setBaseUnitId(unitId);
        return material;
    }

    private static UnitOptionVo unit(Long id, String name, Integer precision) {
        UnitOptionVo unit = new UnitOptionVo();
        unit.setId(id); unit.setUnitName(name); unit.setPrecisionScale(precision);
        return unit;
    }
}
