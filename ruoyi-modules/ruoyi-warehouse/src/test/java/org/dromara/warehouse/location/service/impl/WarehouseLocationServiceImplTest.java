package org.dromara.warehouse.location.service.impl;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.service.DictService;
import org.dromara.warehouse.location.domain.WarehouseLocation;
import org.dromara.warehouse.inventory.mapper.InventoryMapper;
import org.dromara.warehouse.inventory.transaction.mapper.InventoryTransactionMapper;
import org.dromara.warehouse.location.domain.bo.WarehouseLocationBo;
import org.dromara.warehouse.location.mapper.WarehouseLocationMapper;
import org.dromara.warehouse.security.WarehouseAccessService;
import org.dromara.warehouse.warehouse.domain.Warehouse;
import org.dromara.warehouse.warehouse.mapper.WarehouseMapper;
import org.dromara.warehouse.zone.domain.WarehouseZone;
import org.dromara.warehouse.zone.mapper.WarehouseZoneMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** 库位业务规则测试。 */
@ExtendWith(MockitoExtension.class)
class WarehouseLocationServiceImplTest {
    @Mock WarehouseLocationMapper locationMapper; @Mock WarehouseZoneMapper zoneMapper; @Mock WarehouseMapper warehouseMapper;
    @Mock WarehouseAccessService accessService; @Mock DictService dictService; @Mock InventoryMapper inventoryMapper; @Mock InventoryTransactionMapper transactionMapper; @InjectMocks WarehouseLocationServiceImpl service;
    @BeforeEach void setUp() { lenient().when(dictService.getAllDictByDictType("sys_normal_disable")).thenReturn(Map.of("0", "正常", "1", "停用")); lenient().when(accessService.requireEnabledWarehouse(anyLong())).thenReturn(new Warehouse()); lenient().when(locationMapper.insert(any(WarehouseLocation.class))).thenAnswer(i -> { ((WarehouseLocation)i.getArgument(0)).setId(1L); return 1; }); }
    @Test void createLocationSucceeds() { when(zoneMapper.selectById(11L)).thenReturn(zone(1L)); WarehouseLocationBo bo = bo(1L, 11L, "A-01"); assertTrue(service.insertByBo(bo)); }
    @Test void duplicateCodeInSameWarehouseFails() { when(zoneMapper.selectById(11L)).thenReturn(zone(1L)); when(locationMapper.countByCode(1L, "A-01", null)).thenReturn(1L); assertThrows(ServiceException.class, () -> service.insertByBo(bo(1L, 11L, "A-01"))); }
    @Test void crossWarehouseZoneFails() { when(zoneMapper.selectById(11L)).thenReturn(zone(2L)); assertThrows(ServiceException.class, () -> service.insertByBo(bo(1L, 11L, "A-01"))); }
    @Test void sameCodeInDifferentWarehouseSucceeds() { when(zoneMapper.selectById(21L)).thenReturn(zone(2L)); assertTrue(service.insertByBo(bo(2L, 21L, "A-01"))); verify(locationMapper).countByCode(2L, "A-01", null); }
    @Test void locationWithInventoryCannotBeDeleted() { WarehouseLocation location = new WarehouseLocation(); location.setWarehouseId(1L); when(locationMapper.selectById(9L)).thenReturn(location); when(inventoryMapper.existsByLocationIds(any())).thenReturn(true); assertThrows(ServiceException.class, () -> service.deleteWithValidByIds(List.of(9L))); verify(locationMapper, never()).deleteByIds(any(java.util.Collection.class)); }
    @Test void locationWithTransactionCannotBeDeleted() { WarehouseLocation location = new WarehouseLocation(); location.setWarehouseId(1L); when(locationMapper.selectById(9L)).thenReturn(location); when(transactionMapper.existsByLocationIds(any())).thenReturn(true); assertThrows(ServiceException.class, () -> service.deleteWithValidByIds(List.of(9L))); verify(locationMapper, never()).deleteByIds(any(java.util.Collection.class)); }
    private WarehouseZone zone(Long warehouseId) { WarehouseZone z = new WarehouseZone(); z.setWarehouseId(warehouseId); z.setStatus("0"); return z; }
    private WarehouseLocationBo bo(Long warehouseId, Long zoneId, String code) { WarehouseLocationBo bo = new WarehouseLocationBo(); bo.setWarehouseId(warehouseId); bo.setZoneId(zoneId); bo.setLocationCode(code); bo.setLocationName(code); bo.setStatus("0"); return bo; }
}
