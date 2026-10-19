package org.dromara.warehouse.inventory.service.impl;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.warehouse.inventory.domain.Inventory;
import org.dromara.warehouse.inventory.mapper.InventoryMapper;
import org.dromara.warehouse.location.domain.WarehouseLocation;
import org.dromara.warehouse.location.mapper.WarehouseLocationMapper;
import org.dromara.warehouse.material.domain.Material;
import org.dromara.warehouse.material.mapper.MaterialMapper;
import org.dromara.warehouse.security.WarehouseAccessService;
import org.dromara.warehouse.warehouse.domain.Warehouse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 库存余额数值、关系与权限边界测试。 */
@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {
    @Mock InventoryMapper inventoryMapper;
    @Mock WarehouseLocationMapper locationMapper;
    @Mock MaterialMapper materialMapper;
    @Mock WarehouseAccessService accessService;
    @InjectMocks InventoryServiceImpl service;

    @BeforeEach void setUp() {
        Warehouse warehouse = new Warehouse(); warehouse.setId(1L); warehouse.setCompanyId(10L);
        WarehouseLocation location = new WarehouseLocation(); location.setId(2L); location.setWarehouseId(1L);
        Material material = new Material(); material.setId(3L); material.setCompanyId(10L);
        lenient().when(accessService.requireWarehouse(1L)).thenReturn(warehouse);
        lenient().when(locationMapper.selectById(2L)).thenReturn(location);
        lenient().when(materialMapper.selectById(3L)).thenReturn(material);
    }

    @Test void validBalancePasses() { assertDoesNotThrow(() -> service.validateBalance(balance("10", "2"))); }
    @Test void negativeQuantityIsRejected() { assertThrows(ServiceException.class, () -> service.validateBalance(balance("-1", "0"))); }
    @Test void negativeLockedQuantityIsRejected() { assertThrows(ServiceException.class, () -> service.validateBalance(balance("1", "-1"))); }
    @Test void lockedQuantityAboveQuantityIsRejected() { assertThrows(ServiceException.class, () -> service.validateBalance(balance("1", "2"))); }
    @Test void availableQuantityIsCalculated() { assertEquals(new BigDecimal("7.500000"), InventoryServiceImpl.available(new BigDecimal("10.000000"), new BigDecimal("2.500000"))); }
    @Test void crossWarehouseLocationIsRejected() { WarehouseLocation location = new WarehouseLocation(); location.setWarehouseId(9L); when(locationMapper.selectById(2L)).thenReturn(location); assertThrows(ServiceException.class, () -> service.validateRelations(1L, 2L, 3L)); }
    @Test void crossCompanyMaterialIsRejected() { Material material = new Material(); material.setCompanyId(11L); when(materialMapper.selectById(3L)).thenReturn(material); assertThrows(ServiceException.class, () -> service.validateRelations(1L, 2L, 3L)); }
    @Test void relationValidationUsesWarehouseAccessBoundary() { service.validateRelations(1L, 2L, 3L); verify(accessService).requireWarehouse(1L); }

    private Inventory balance(String quantity, String locked) {
        Inventory inventory = new Inventory(); inventory.setWarehouseId(1L); inventory.setLocationId(2L); inventory.setMaterialId(3L);
        inventory.setQuantity(new BigDecimal(quantity)); inventory.setLockedQuantity(new BigDecimal(locked)); return inventory;
    }
}
