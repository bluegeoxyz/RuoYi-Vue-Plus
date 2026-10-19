package org.dromara.warehouse.zone.service.impl;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.service.DictService;
import org.dromara.warehouse.location.mapper.WarehouseLocationMapper;
import org.dromara.warehouse.security.WarehouseAccessService;
import org.dromara.warehouse.warehouse.domain.Warehouse;
import org.dromara.warehouse.warehouse.mapper.WarehouseMapper;
import org.dromara.warehouse.zone.domain.WarehouseZone;
import org.dromara.warehouse.zone.domain.bo.WarehouseZoneBo;
import org.dromara.warehouse.zone.mapper.WarehouseZoneMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** 库区业务规则测试。 */
@ExtendWith(MockitoExtension.class)
class WarehouseZoneServiceImplTest {
    @Mock WarehouseZoneMapper zoneMapper; @Mock WarehouseLocationMapper locationMapper; @Mock WarehouseMapper warehouseMapper;
    @Mock WarehouseAccessService accessService; @Mock DictService dictService; @InjectMocks WarehouseZoneServiceImpl service;
    @BeforeEach void setUp() { lenient().when(dictService.getAllDictByDictType("sys_normal_disable")).thenReturn(Map.of("0", "正常", "1", "停用")); lenient().when(accessService.requireEnabledWarehouse(anyLong())).thenReturn(new Warehouse()); lenient().when(zoneMapper.insert(any(WarehouseZone.class))).thenAnswer(i -> { ((WarehouseZone)i.getArgument(0)).setId(1L); return 1; }); }
    @Test void createZoneSucceeds() { WarehouseZoneBo bo = bo(1L, "A"); assertTrue(service.insertByBo(bo)); assertEquals(1L, bo.getId()); }
    @Test void duplicateCodeInSameWarehouseFails() { when(zoneMapper.countByCode(1L, "A", null)).thenReturn(1L); assertThrows(ServiceException.class, () -> service.insertByBo(bo(1L, "A"))); }
    @Test void sameCodeInDifferentWarehouseSucceeds() { assertTrue(service.insertByBo(bo(2L, "A"))); verify(zoneMapper).countByCode(2L, "A", null); }
    @Test void zoneWithLocationCannotBeDeleted() { WarehouseZone zone = new WarehouseZone(); zone.setWarehouseId(1L); when(zoneMapper.selectById(1L)).thenReturn(zone); when(locationMapper.existsByZoneIds(any())).thenReturn(true); assertThrows(ServiceException.class, () -> service.deleteWithValidByIds(List.of(1L))); }
    private WarehouseZoneBo bo(Long warehouseId, String code) { WarehouseZoneBo bo = new WarehouseZoneBo(); bo.setWarehouseId(warehouseId); bo.setZoneCode(code); bo.setZoneName(code + "区"); bo.setStatus("0"); return bo; }
}
