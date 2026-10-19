package org.dromara.warehouse.warehouse.service.impl;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.service.DictService;
import org.dromara.property.api.ProjectReferenceDTO;
import org.dromara.property.api.ProjectReferenceService;
import org.dromara.warehouse.security.WarehouseAccessService;
import org.dromara.warehouse.inventory.mapper.InventoryMapper;
import org.dromara.warehouse.inventory.transaction.mapper.InventoryTransactionMapper;
import org.dromara.warehouse.warehouse.domain.Warehouse;
import org.dromara.warehouse.warehouse.domain.bo.WarehouseBo;
import org.dromara.warehouse.warehouse.mapper.WarehouseMapper;
import org.dromara.warehouse.zone.mapper.WarehouseZoneMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** 仓库核心业务规则测试。 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WarehouseServiceImplTest {
    @Mock WarehouseMapper warehouseMapper;
    @Mock WarehouseZoneMapper zoneMapper;
    @Mock WarehouseAccessService accessService;
    @Mock ProjectReferenceService projectReferenceService;
    @Mock DictService dictService;
    @Mock InventoryMapper inventoryMapper;
    @Mock InventoryTransactionMapper transactionMapper;
    @InjectMocks WarehouseServiceImpl service;

    @BeforeEach void setUp() {
        when(dictService.getAllDictByDictType("wm_warehouse_scope")).thenReturn(Map.of("COMPANY", "公司仓", "PROJECT", "项目仓"));
        when(dictService.getAllDictByDictType("sys_normal_disable")).thenReturn(Map.of("0", "正常", "1", "停用"));
        when(warehouseMapper.insert(any(Warehouse.class))).thenAnswer(inv -> { ((Warehouse) inv.getArgument(0)).setId(1L); return 1; });
    }

    @Test void createCompanyWarehouseSucceeds() {
        WarehouseBo bo = base("COMPANY");
        assertTrue(service.insertByBo(bo));
        assertEquals(1L, bo.getId());
        verify(accessService).validateCompany(10L);
        verify(accessService, never()).validateProject(anyLong());
    }

    @Test void createProjectWarehouseSucceeds() {
        WarehouseBo bo = base("PROJECT"); bo.setProjectId(20L);
        when(accessService.validateProject(20L)).thenReturn(new ProjectReferenceDTO(20L, "P20", "项目20", "0"));
        assertTrue(service.insertByBo(bo));
        verify(accessService).validateProject(20L);
    }

    @Test void projectWarehouseRequiresProject() {
        assertThrows(ServiceException.class, () -> service.insertByBo(base("PROJECT")));
        verify(warehouseMapper, never()).insert(any(Warehouse.class));
    }

    @Test void companyWarehouseRejectsProject() {
        WarehouseBo bo = base("COMPANY"); bo.setProjectId(20L);
        assertThrows(ServiceException.class, () -> service.insertByBo(bo));
    }

    @Test void duplicateWarehouseCodeFails() {
        when(warehouseMapper.countByCode(10L, "WH-A", null)).thenReturn(1L);
        assertThrows(ServiceException.class, () -> service.insertByBo(base("COMPANY")));
    }

    @Test void warehouseWithZoneCannotBeDeleted() {
        when(accessService.requireWarehouse(1L)).thenReturn(new Warehouse());
        when(zoneMapper.existsByWarehouseIds(any())).thenReturn(true);
        assertThrows(ServiceException.class, () -> service.deleteWithValidByIds(List.of(1L)));
        verify(warehouseMapper, never()).deleteByIds(any(java.util.Collection.class));
    }

    @Test void warehouseWithInventoryCannotBeDeleted() {
        when(accessService.requireWarehouse(1L)).thenReturn(new Warehouse());
        when(inventoryMapper.existsByWarehouseIds(any())).thenReturn(true);
        assertThrows(ServiceException.class, () -> service.deleteWithValidByIds(List.of(1L)));
        verify(warehouseMapper, never()).deleteByIds(any(java.util.Collection.class));
    }

    @Test void warehouseWithTransactionCannotBeDeleted() {
        when(accessService.requireWarehouse(1L)).thenReturn(new Warehouse());
        when(transactionMapper.existsByWarehouseIds(any())).thenReturn(true);
        assertThrows(ServiceException.class, () -> service.deleteWithValidByIds(List.of(1L)));
        verify(warehouseMapper, never()).deleteByIds(any(java.util.Collection.class));
    }

    private WarehouseBo base(String scope) {
        WarehouseBo bo = new WarehouseBo(); bo.setCompanyId(10L); bo.setWarehouseScope(scope);
        bo.setWarehouseCode(" wh-a "); bo.setWarehouseName(" A仓 "); bo.setStatus("0"); return bo;
    }
}
