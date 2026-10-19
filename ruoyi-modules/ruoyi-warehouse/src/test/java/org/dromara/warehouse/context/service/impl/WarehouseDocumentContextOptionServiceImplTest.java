package org.dromara.warehouse.context.service.impl;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.security.business.BusinessContext;
import org.dromara.common.security.business.BusinessContextAccessService;
import org.dromara.common.security.business.ReferenceType;
import org.dromara.warehouse.inventory.domain.Inventory;
import org.dromara.warehouse.inventory.mapper.InventoryMapper;
import org.dromara.warehouse.location.domain.WarehouseLocation;
import org.dromara.warehouse.location.mapper.WarehouseLocationMapper;
import org.dromara.warehouse.material.domain.Material;
import org.dromara.warehouse.material.category.domain.vo.MaterialCategoryOptionVo;
import org.dromara.warehouse.material.category.mapper.MaterialCategoryMapper;
import org.dromara.warehouse.material.mapper.MaterialMapper;
import org.dromara.warehouse.security.WarehouseAccessService;
import org.dromara.warehouse.unit.mapper.UnitMapper;
import org.dromara.warehouse.warehouse.domain.Warehouse;
import org.dromara.warehouse.warehouse.mapper.WarehouseMapper;
import org.dromara.warehouse.zone.domain.WarehouseZone;
import org.dromara.warehouse.zone.mapper.WarehouseZoneMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseDocumentContextOptionServiceImplTest {
    @Mock BusinessContextAccessService contextAccessService;
    @Mock WarehouseAccessService warehouseAccessService;
    @Mock WarehouseMapper warehouseMapper;
    @Mock WarehouseZoneMapper zoneMapper;
    @Mock WarehouseLocationMapper locationMapper;
    @Mock MaterialCategoryMapper categoryMapper;
    @Mock MaterialMapper materialMapper;
    @Mock UnitMapper unitMapper;
    @Mock InventoryMapper inventoryMapper;
    @InjectMocks WarehouseDocumentContextOptionServiceImpl service;

    @Test
    void eachDocumentSceneUsesItsOwnBusinessContext() {
        when(warehouseAccessService.accessibleWarehouseIds()).thenReturn(List.of());
        for (BusinessContext context : List.of(BusinessContext.OUTBOUND_MAINTAIN,
            BusinessContext.TRANSFER_MAINTAIN, BusinessContext.STOCKTAKE_MAINTAIN,
            BusinessContext.INBOUND_QUERY, BusinessContext.OUTBOUND_QUERY,
            BusinessContext.TRANSFER_QUERY, BusinessContext.STOCKTAKE_QUERY)) {
            assertTrue(service.selectWarehouses(context).isEmpty());
            verify(contextAccessService).requireAccess(context, ReferenceType.WAREHOUSE);
        }
        verifyNoInteractions(warehouseMapper);
    }

    @Test
    void contextDenialStopsWarehouseScopeLookup() {
        doThrow(new cn.dev33.satoken.exception.NotPermissionException("拒绝"))
            .when(contextAccessService).requireAccess(BusinessContext.OUTBOUND_MAINTAIN, ReferenceType.WAREHOUSE);
        assertThrows(cn.dev33.satoken.exception.NotPermissionException.class,
            () -> service.selectWarehouses(BusinessContext.OUTBOUND_MAINTAIN));
        verifyNoInteractions(warehouseAccessService, warehouseMapper);
    }

    @Test
    void projectOrDepartmentScopeDenialStopsInventoryLookup() {
        doThrow(new ServiceException("无仓库范围")).when(warehouseAccessService).requireWarehouse(10L);
        assertThrows(ServiceException.class,
            () -> service.selectInventory(BusinessContext.OUTBOUND_MAINTAIN, 10L, 20L, 30L));
        verifyNoInteractions(locationMapper, zoneMapper, materialMapper, inventoryMapper);
    }

    @Test
    void rejectsLocationBelongingToAnotherWarehouse() {
        when(warehouseAccessService.requireWarehouse(10L)).thenReturn(warehouse(10L, 100L));
        WarehouseLocation location = new WarehouseLocation();
        location.setId(20L); location.setWarehouseId(11L); location.setStatus("0");
        when(locationMapper.selectById(20L)).thenReturn(location);

        assertThrows(ServiceException.class,
            () -> service.selectInventory(BusinessContext.TRANSFER_MAINTAIN, 10L, 20L, 30L));
        verifyNoInteractions(materialMapper, inventoryMapper);
    }

    @Test
    void rejectsMaterialBelongingToAnotherCompany() {
        when(warehouseAccessService.requireWarehouse(10L)).thenReturn(warehouse(10L, 100L));
        when(locationMapper.selectById(20L)).thenReturn(location(20L, 10L, 21L));
        when(zoneMapper.selectById(21L)).thenReturn(zone(21L, 10L));
        Material material = new Material(); material.setId(30L); material.setCompanyId(101L); material.setStatus("0");
        when(materialMapper.selectById(30L)).thenReturn(material);

        assertThrows(ServiceException.class,
            () -> service.selectInventory(BusinessContext.STOCKTAKE_MAINTAIN, 10L, 20L, 30L));
        verify(warehouseAccessService).assertMaterialCompanyAccess(100L);
        verifyNoInteractions(inventoryMapper);
    }

    @Test
    void exactInventoryDimensionReturnsAvailableQuantity() {
        when(warehouseAccessService.requireWarehouse(10L)).thenReturn(warehouse(10L, 100L));
        when(locationMapper.selectById(20L)).thenReturn(location(20L, 10L, 21L));
        when(zoneMapper.selectById(21L)).thenReturn(zone(21L, 10L));
        Material material = new Material(); material.setId(30L); material.setCompanyId(100L); material.setStatus("0");
        when(materialMapper.selectById(30L)).thenReturn(material);
        Inventory inventory = new Inventory(); inventory.setQuantity(new BigDecimal("12.5")); inventory.setLockedQuantity(new BigDecimal("2.5"));
        when(inventoryMapper.selectBalance(10L, 20L, 30L)).thenReturn(inventory);

        var result = service.selectInventory(BusinessContext.OUTBOUND_MAINTAIN, 10L, 20L, 30L);

        assertEquals(new BigDecimal("10.0"), result.availableQuantity());
        verify(inventoryMapper).selectBalance(10L, 20L, 30L);
        verify(contextAccessService).requireAccess(BusinessContext.OUTBOUND_MAINTAIN, ReferenceType.INVENTORY);
    }

    @Test
    void absentBalanceReturnsZeroWithoutBroadInventoryQuery() {
        when(warehouseAccessService.requireWarehouse(10L)).thenReturn(warehouse(10L, 100L));
        when(locationMapper.selectById(20L)).thenReturn(location(20L, 10L, 21L));
        when(zoneMapper.selectById(21L)).thenReturn(zone(21L, 10L));
        Material material = new Material(); material.setId(30L); material.setCompanyId(100L); material.setStatus("0");
        when(materialMapper.selectById(30L)).thenReturn(material);

        var result = service.selectInventory(BusinessContext.TRANSFER_MAINTAIN, 10L, 20L, 30L);

        assertEquals(BigDecimal.ZERO, result.quantity());
        assertEquals(BigDecimal.ZERO, result.availableQuantity());
    }

    @Test
    void inventoryQueryCategoriesAreLimitedToAuthorizedWarehouseCompanies() {
        when(warehouseAccessService.accessibleWarehouseIds()).thenReturn(List.of(10L));
        when(warehouseMapper.selectOptions(List.of(10L))).thenReturn(List.of(warehouseOption(10L, 100L)));
        MaterialCategoryOptionVo root = category(1L, 100L, 0L);
        MaterialCategoryOptionVo child = category(2L, 100L, 1L);
        when(categoryMapper.selectOptions(java.util.Set.of(100L), 100L)).thenReturn(List.of(root, child));

        List<MaterialCategoryOptionVo> result = service.selectMaterialCategories(
            BusinessContext.INVENTORY_QUERY, 100L);

        assertEquals(1, result.size());
        assertEquals(List.of(child), result.getFirst().getChildren());
        verify(contextAccessService).requireAccess(BusinessContext.INVENTORY_QUERY, ReferenceType.MATERIAL_CATEGORY);
    }

    @Test
    void inventoryQueryRejectsCategoryCompanyWithoutAuthorizedWarehouse() {
        when(warehouseAccessService.accessibleWarehouseIds()).thenReturn(List.of(10L));
        when(warehouseMapper.selectOptions(List.of(10L))).thenReturn(List.of(warehouseOption(10L, 100L)));

        assertThrows(ServiceException.class, () -> service.selectMaterialCategories(
            BusinessContext.INVENTORY_QUERY, 101L));

        verifyNoInteractions(categoryMapper);
    }

    private static Warehouse warehouse(Long id, Long companyId) {
        Warehouse value = new Warehouse(); value.setId(id); value.setCompanyId(companyId); return value;
    }

    private static WarehouseLocation location(Long id, Long warehouseId, Long zoneId) {
        WarehouseLocation value = new WarehouseLocation(); value.setId(id); value.setWarehouseId(warehouseId);
        value.setZoneId(zoneId); value.setStatus("0"); return value;
    }

    private static WarehouseZone zone(Long id, Long warehouseId) {
        WarehouseZone value = new WarehouseZone(); value.setId(id); value.setWarehouseId(warehouseId);
        value.setStatus("0"); return value;
    }

    private static org.dromara.warehouse.warehouse.domain.vo.WarehouseOptionVo warehouseOption(
        Long id, Long companyId) {
        var value = new org.dromara.warehouse.warehouse.domain.vo.WarehouseOptionVo();
        value.setId(id); value.setCompanyId(companyId); return value;
    }

    private static MaterialCategoryOptionVo category(Long id, Long companyId, Long parentId) {
        MaterialCategoryOptionVo value = new MaterialCategoryOptionVo();
        value.setId(id); value.setCompanyId(companyId); value.setParentId(parentId); return value;
    }
}
