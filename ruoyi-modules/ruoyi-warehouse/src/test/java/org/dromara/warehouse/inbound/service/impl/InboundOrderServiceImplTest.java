package org.dromara.warehouse.inbound.service.impl;

import org.dromara.common.core.exception.ServiceException;
import org.dromara.property.api.ProjectReferenceService;
import org.dromara.warehouse.inbound.domain.InboundOrder;
import org.dromara.warehouse.inbound.mapper.InboundOrderItemMapper;
import org.dromara.warehouse.inbound.mapper.InboundOrderMapper;
import org.dromara.warehouse.inventory.service.InventoryMutationService;
import org.dromara.warehouse.location.mapper.WarehouseLocationMapper;
import org.dromara.warehouse.material.mapper.MaterialMapper;
import org.dromara.warehouse.security.WarehouseAccessService;
import org.dromara.warehouse.unit.mapper.UnitMapper;
import org.dromara.warehouse.warehouse.domain.Warehouse;
import org.dromara.warehouse.zone.mapper.WarehouseZoneMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 入库单状态与权限边界测试。 */
@ExtendWith(MockitoExtension.class)
class InboundOrderServiceImplTest {
    @Mock InboundOrderMapper orderMapper; @Mock InboundOrderItemMapper itemMapper; @Mock WarehouseAccessService accessService;
    @Mock WarehouseLocationMapper locationMapper; @Mock WarehouseZoneMapper zoneMapper; @Mock MaterialMapper materialMapper;
    @Mock UnitMapper unitMapper; @Mock InventoryMutationService mutationService; @Mock ProjectReferenceService projectReferenceService;
    @InjectMocks InboundOrderServiceImpl service;

    @Test void confirmedOrderCannotBeConfirmedAgain() {
        InboundOrder order = order("CONFIRMED"); when(orderMapper.selectForUpdate(1L)).thenReturn(order);
        when(accessService.requireEnabledWarehouse(2L)).thenReturn(new Warehouse());
        assertThrows(ServiceException.class, () -> service.confirm(1L));
        verifyNoInteractions(mutationService);
    }

    @Test void completedOrderCannotBeCompletedAgain() {
        InboundOrder order = order("COMPLETED"); when(orderMapper.selectForUpdate(1L)).thenReturn(order);
        when(accessService.requireEnabledWarehouse(2L)).thenReturn(new Warehouse());
        assertThrows(ServiceException.class, () -> service.complete(1L));
        verifyNoInteractions(mutationService);
    }

    @Test void completedOrderCannotBeCancelled() {
        InboundOrder order = order("COMPLETED"); when(orderMapper.selectForUpdate(1L)).thenReturn(order);
        assertThrows(ServiceException.class, () -> service.cancel(1L));
        verify(orderMapper, never()).cancelCas(anyLong());
    }

    @Test void detailChecksWarehouseBeforeJoinedQuery() {
        InboundOrder order = order("DRAFT"); when(orderMapper.selectById(1L)).thenReturn(order);
        doThrow(new ServiceException("denied")).when(accessService).requireWarehouse(2L);
        assertThrows(ServiceException.class, () -> service.queryById(1L));
        verify(orderMapper, never()).selectInboundById(anyLong());
    }

    @Test void completeIsTransactional() throws Exception {
        assertNotNull(InboundOrderServiceImpl.class.getMethod("complete", Long.class).getAnnotation(Transactional.class));
    }

    private InboundOrder order(String status) { InboundOrder order = new InboundOrder(); order.setId(1L); order.setWarehouseId(2L); order.setStatus(status); return order; }
}
